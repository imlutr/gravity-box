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

package ro.luca1152.gravitybox.utils.ads

import com.badlogic.gdx.Gdx
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.systems.game.SkipLevelEvent
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.ui.panes.RewardedAddErrorPane

class MyRewardedAds(private val context: Context) {
    // Injected objects
    private val adsController: AdsController? = context.injectNullable()
    private val gameRules: GameRules = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val eventQueue: EventQueue = context.inject()

    fun init() {
        adsController?.rewardedAdEventListener = object : RewardedAdEventListener {
            override fun onRewardedEvent(type: String, amount: Int) {
                Gdx.app.log("AdMob", "Rewarding player with [$type, $amount].")
                gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN = gameRules.TIME_DELAY_BETWEEN_REWARDED_ADS
                eventQueue.add(SkipLevelEvent())
            }

            override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
                menuOverlayStage.addActor(RewardedAddErrorPane(context, errorCode))
            }
        }
    }
}