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

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

/** Handles deciding when an interstitial ad should be shown. */
class InterstitialAdsSystem(context: Context) : EntitySystem() {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    /** How many ads were shown in the current session. */
    private var showAdsTimer = gameRules.TIME_DELAY_BETWEEN_INTERSTITIAL_ADS
    private var lastAdShownAtLevelId = -gameRules.LEVELS_DELAY_BETWEEN_INTERSTITIAL_ADS

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun update(deltaTime: Float) {
        if (gameRules.IS_AD_FREE) return
        if (gameRules.SHOULD_SHOW_INTERSTITIAL_AD) {
            // An interstitial ad may not be available at first
            lastAdShownAtLevelId = levelEntity.level.levelId
            return
        }
        if (levelEntity.level.levelId - lastAdShownAtLevelId < gameRules.LEVELS_DELAY_BETWEEN_INTERSTITIAL_ADS) {
            showAdsTimer = gameRules.TIME_DELAY_BETWEEN_INTERSTITIAL_ADS
            return
        }
        updateShowAdsVariable(deltaTime)
    }

    private fun updateShowAdsVariable(deltaTime: Float) {
        if (showAdsTimer <= 0f) {
            gameRules.SHOULD_SHOW_INTERSTITIAL_AD = true
            showAdsTimer = gameRules.TIME_DELAY_BETWEEN_INTERSTITIAL_ADS
            lastAdShownAtLevelId = levelEntity.level.levelId
        } else {
            showAdsTimer -= deltaTime
        }
    }
}