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
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboardController
import kotlin.math.min

class ShowNextLevelEvent : Event
class ShowNextLevelSystem(
    private val context: Context,
    private val playScreen: PlayScreen
) : EventSystem<ShowNextLevelEvent>(context.inject(), ShowNextLevelEvent::class) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val gameStage: GameStage = context.inject()
    private val gameShotsLeaderboardController: GameShotsLeaderboardController = context.inject()
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEvent(event: ShowNextLevelEvent, deltaTime: Float) {
        updateLeaderboard()
        showNextLevel()
        updateGameRules()
    }

    private fun updateLeaderboard() {
        val shots = levelEntity.map.shots
        levelEntity.level.run {
            if (gameRules.getGameLevelHighscore(levelId) <= shots)
                return

            gameShotsLeaderboardController.incrementPlayerCountForShots(levelId, shots)
            if (gameRules.getGameLevelHighscore(levelId) != gameRules.DEFAULT_HIGHSCORE_VALUE &&
                gameRules.getGameLevelHighscore(levelId) != gameRules.SKIPPED_LEVEL_SCORE_VALUE
            ) {
                gameShotsLeaderboardController.decrementPlayerCountForShots(levelId, gameRules.getGameLevelHighscore(levelId))
            }
            gameRules.setGameLevelHighscore(levelId, shots)
        }
    }

    private fun showNextLevel() {
        levelEntity.level.run {
            levelId = min(levelEntity.level.levelId + 1, gameRules.LEVEL_COUNT)
            isRestarting = true
            loadMap = true
            forceUpdateMap = true
        }
        levelEntity.map.run {
            forceCenterCameraOnPlayer = true
            rank = -1
            resetPassengers()
        }
        gameStage.addAction(
            Actions.sequence(
                Actions.fadeOut(0f),
                Actions.fadeIn(.25f, Interpolation.pow3In),
                Actions.run {
                    levelEntity.level.isRestarting = false
                }
            )
        )
        playScreen.run {
            finishOverlay.hide()
            menuOverlay.shouldUpdateLevelLabel = true
        }
        eventQueue.run {
            add(PromptUserToRateEvent())
            add(ShowInterstitialAdEvent())
        }
    }

    private fun updateGameRules() {
        if (context.injectNullable<GameShotsLeaderboard>() != null) {
            gameRules.DID_SHOW_GUIDE_BETWEEN_LEVELS = true
        }
    }
}