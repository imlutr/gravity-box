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

package ro.luca1152.gravitybox.systems.game

import com.badlogic.ashley.core.EntitySystem
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules

class InterstitialAdsSystem(context: Context) : EntitySystem() {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    /** How many ads were shown in the current session. */
    private var adsShownCount = 0
    private var showAdsTimer = gameRules.TIME_DELAY_BETWEEN_INTERSTITIAL_ADS

    override fun update(deltaTime: Float) {
        if (gameRules.IS_AD_FREE) return
        if (adsShownCount + 1 > gameRules.MAX_INTERSTITIAL_ADS_PER_SESSION) return

        println("$showAdsTimer | ${gameRules.SHOULD_SHOW_INTERSTITIAL_AD} | ${adsShownCount}")
        if (showAdsTimer <= 0f) {
            if (!gameRules.SHOULD_SHOW_INTERSTITIAL_AD) {
                gameRules.SHOULD_SHOW_INTERSTITIAL_AD = true
                adsShownCount++
            } else if (!gameRules.SHOULD_SHOW_INTERSTITIAL_AD) {
                // The ad was shown, so the timer should be restarted
                showAdsTimer = gameRules.TIME_DELAY_BETWEEN_INTERSTITIAL_ADS
            }
        } else {
            showAdsTimer -= deltaTime
        }
    }
}