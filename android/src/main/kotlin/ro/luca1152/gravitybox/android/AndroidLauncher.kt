/*
 * This file is part of Gravity Box.
 *
 * Gravity Box is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gravity Box is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gravity Box.  If not, see <https://www.gnu.org/licenses/>.
 */

package ro.luca1152.gravitybox.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.pay.android.googlebilling.PurchaseManagerGoogleBilling
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import ro.luca1152.gravitybox.BuildConfig
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ads.AdsController


/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    private lateinit var adRequest: AdRequest
    private lateinit var interstitialAd: InterstitialAd
    private lateinit var rewardedVideoAd: RewardedVideoAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Don't dim the screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize AdMob
        MobileAds.initialize(this, BuildConfig.AD_MOB_APP_ID)

        // Test devices
        adRequest = AdRequest.Builder()
            .addTestDevice("782BFD7102248952BFA1C5BD83FDE48C")
            .addTestDevice("FE5037566C41F6046E5DD6F3BA34694C")
            .build()

        // Initialize interstitial ads
        interstitialAd = initializeInterstitialAds()

        // Initialize the game
        initialize(MyGame().apply {
            // gdx-pay
            purchaseManager = PurchaseManagerGoogleBilling(this@AndroidLauncher)

            // AdMob
            adsController = initializeAdsController()

            // Initialize rewarded video ads
            rewardedVideoAd = initializeRewardedVideoAds(adsController)
            loadRewardedVideoAd()
        }, AndroidApplicationConfiguration())
    }

    private fun initializeInterstitialAds() = InterstitialAd(this).apply {
        adUnitId = BuildConfig.AD_MOB_INTERSTITIAL_AD_UNIT_ID
        loadAd(adRequest)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                // Load the next interstitial
                loadAd(adRequest)
            }

            override fun onAdFailedToLoad(p0: Int) {
                Gdx.app.log("AdMob", "Ad failed to load. Error code: $p0.")
            }
        }
    }

    private fun initializeAdsController() = object : AdsController() {
        override fun showInterstitialAd() {
            this@AndroidLauncher.runOnUiThread {
                if (interstitialAd.isLoaded) {
                    Gdx.app.log("AdMob", "Showing interstitial ad.")
                    interstitialAd.show()
                } else {
                    Gdx.app.log("AdMob", "Interstitial ad is not loaded yet.")
                }
            }
        }

        override fun isInterstitialAdLoaded(): Boolean {
            var isLoaded = false
            this@AndroidLauncher.runOnUiThread {
                isLoaded = interstitialAd.isLoaded
            }
            return isLoaded
        }

        override fun loadRewardedAd() {
            this@AndroidLauncher.runOnUiThread {
                loadRewardedVideoAd()
            }
        }

        override fun showRewardedAd() {
            this@AndroidLauncher.runOnUiThread {
                if (rewardedVideoAd.isLoaded) {
                    rewardedVideoAd.show()
                } else {
                    isShowingRewardedAdScheduled = true
                }
            }
        }

        override fun isRewardedAdLoaded(): Boolean {
            var isLoaded = false
            this@AndroidLauncher.runOnUiThread {
                isLoaded = rewardedVideoAd.isLoaded
            }
            return isLoaded
        }

        override fun isNetworkConnected(): Boolean {
            val connectionType = getConnectionType(this@AndroidLauncher)
            return connectionType == 1 || connectionType == 2
        }
    }

    /**
     * Returns:
     * 0: No internet available
     * 1: Cellular (mobile data, 3G/4G/LTE, etc.)
     * 2: Wi-Fi
     */
    @Suppress("DEPRECATION")
    fun getConnectionType(context: Context): Int {
        var result = 0
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = 2
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = 1
                    }
                }
            }
        }
        return result
    }

    private fun loadRewardedVideoAd() {
        rewardedVideoAd.loadAd(BuildConfig.AD_MOB_REWARDED_AD_UNIT_ID, adRequest)
    }

    private fun initializeRewardedVideoAds(adsController: AdsController) = MobileAds.getRewardedVideoAdInstance(this).apply {
        val rewardedAdEventListener = adsController.rewardedAdEventListener
        rewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onRewarded(p0: RewardItem) {
                rewardedAdEventListener?.onRewardedEvent(p0.type, p0.amount)
            }

            override fun onRewardedVideoAdLoaded() {
                Gdx.app.log("AdMob", "Rewarded video loaded.")
                if (adsController.isShowingRewardedAdScheduled) {
                    adsController.isShowingRewardedAdScheduled = false
                    show()
                }
            }

            override fun onRewardedVideoAdOpened() {
                Gdx.app.log("AdMob", "Rewarded video opened.")
            }

            override fun onRewardedVideoStarted() {
                Gdx.app.log("AdMob", "Rewarded video started.")
            }

            override fun onRewardedVideoAdClosed() {
                Gdx.app.log("AdMob", "Rewarded video closed.")
                rewardedAdEventListener?.onRewardedVideoAdClosedEvent()
                loadRewardedVideoAd()
            }

            override fun onRewardedVideoCompleted() {
                Gdx.app.log("AdMob", "Rewarded video completed.")
                loadRewardedVideoAd()
            }

            override fun onRewardedVideoAdLeftApplication() {
                Gdx.app.log("AdMob", "Left the application while watching a rewarded video.")
            }

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                Gdx.app.log("AdMob", "Rewarded video ad failed to load. Error code: $p0.")
            }
        }
    }

    override fun onResume() {
        rewardedVideoAd.resume(this)
        super.onResume()
    }

    override fun onPause() {
        rewardedVideoAd.pause(this)
        super.onPause()
    }

    override fun onDestroy() {
        rewardedVideoAd.destroy(this)
        super.onDestroy()
    }
}
