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
import ro.luca1152.gravitybox.BuildConfig
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ads.AdsController


/** Launches the Android application. */
class AndroidLauncher : AndroidApplication() {
    private lateinit var interstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Don't dim the screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize AdMob
        MobileAds.initialize(this, BuildConfig.AD_MOB_APP_ID)

        // Test devices
        val adRequest = AdRequest.Builder()
            .addTestDevice("782BFD7102248952BFA1C5BD83FDE48C")
            .build()

        // Initialize the interstitial ad
        interstitialAd = InterstitialAd(this).apply {
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

        // Initialize the game
        initialize(MyGame().apply {
            // gdx-pay
            purchaseManager = PurchaseManagerGoogleBilling(this@AndroidLauncher)

            // AdMob
            adsController = object : AdsController {
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

                override fun isNetworkConnected(): Boolean {
                    val connectionType = getConnectionType(this@AndroidLauncher)
                    return connectionType == 1 || connectionType == 2
                }
            }
        }, AndroidApplicationConfiguration())
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
}
