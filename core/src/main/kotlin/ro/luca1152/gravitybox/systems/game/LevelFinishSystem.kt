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
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.ads.AdsController
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors

/** Handles what happens when a level is finished. */
class LevelFinishSystem(
    context: Context,
    private val restartLevelWhenFinished: Boolean = false,
    private val playScreen: PlayScreen? = null
) : EntitySystem() {
    // Injected objects
    private val uiStage: UIStage = context.inject()
    private val gameStage: GameStage = context.inject()
    private val gameRules: GameRules = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val adsController: AdsController? = context.injectNullable()

    // Entities
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    // The color scheme is the one that tells whether the level was finished: if the current color scheme
    // is the same as the dark color scheme, then it means that the level was finished. I should change
    // this in the future.
    private val colorSchemeIsFullyTransitioned
        get() = (Colors.useDarkTheme && Colors.gameColor.approxEqualTo(Colors.LightTheme.game57))
                || (!Colors.useDarkTheme && Colors.gameColor.approxEqualTo(Colors.DarkTheme.game95))
    private val levelIsFinished
        get() = playerEntity.player.isInsideFinishPoint && colorSchemeIsFullyTransitioned

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelIsFinished)
            return
        promptUserToRate()
        handleLevelFinish()
        playScreen?.shouldUpdateLevelLabel = true
    }

    private fun handleLevelFinish() {
        if (levelEntity.level.isRestarting) return

        if (restartLevelWhenFinished)
            levelEntity.level.restartLevel = true
        else {
            gameRules.HIGHEST_FINISHED_LEVEL = Math.max(gameRules.HIGHEST_FINISHED_LEVEL, levelEntity.level.levelId)
            levelEntity.level.levelId = Math.min(levelEntity.level.levelId + 1, gameRules.LEVEL_COUNT)
            levelEntity.level.isRestarting = true
            levelEntity.level.run {
                loadMap = true
                forceUpdateMap = true
            }
            levelEntity.map.run {
                forceCenterCameraOnPlayer = true
                resetPassengers()
            }
            showInterstitialAd()
            gameStage.addAction(
                Actions.sequence(
                    Actions.fadeOut(0f),
                    Actions.fadeIn(.25f, Interpolation.pow3In),
                    Actions.run {
                        levelEntity.level.isRestarting = false
                    }
                )
            )
        }
    }

    private fun promptUserToRate() {
        if (gameRules.SHOULD_SHOW_INTERSTITIAL_AD && levelEntity.level.levelId != gameRules.MIN_FINISHED_LEVELS_TO_SHOW_RATE_PROMPT) return
        if (playScreen == null) return
        if (gameRules.DID_RATE_THE_GAME) return
        if (gameRules.NEVER_PROMPT_USER_TO_RATE_THE_GAME) return
        if (levelEntity.level.levelId < gameRules.MIN_FINISHED_LEVELS_TO_SHOW_RATE_PROMPT) return
        if (gameRules.MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN != 0f &&
            gameRules.PLAY_TIME < gameRules.MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN
        ) return
        uiStage.addAction(Actions.sequence(
            Actions.delay(.25f),
            Actions.run { menuOverlayStage.addActor(playScreen.rateGamePromptPopUp) }
        ))
    }

    private fun showInterstitialAd() {
        if (!gameRules.IS_MOBILE) return
        if (gameRules.IS_AD_FREE) return
        if (!gameRules.SHOULD_SHOW_INTERSTITIAL_AD) return
        if (levelEntity.level.levelId == gameRules.MIN_FINISHED_LEVELS_TO_SHOW_RATE_PROMPT) return
        if (!adsController!!.isNetworkConnected()) return
        if (!adsController.isInterstitialAdLoaded()) return
        adsController.showInterstitialAd()
        gameRules.SHOULD_SHOW_INTERSTITIAL_AD = false
    }
}