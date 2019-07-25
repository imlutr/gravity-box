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
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.inject.Context
import pl.mk5.gdx.fireapp.GdxFIRAnalytics
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

class SkipLevelEvent : Event
class SkipLevelSystem(context: Context) : EventSystem<SkipLevelEvent>(context.inject(), SkipLevelEvent::class) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val gameStage: GameStage = context.inject()
    private val eventQueue: EventQueue = context.inject()
    private val playScreen: PlayScreen = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEvent(event: SkipLevelEvent, deltaTime: Float) {
        skipLevel()
    }

    private fun skipLevel() {
        if (levelEntity.level.levelId == gameRules.LEVEL_COUNT) {
            return
        }

        if (gameRules.IS_MOBILE) {
            GdxFIRAnalytics.inst().logEvent("skip_level", mapOf(Pair("level_id", "game/${levelEntity.level.levelId}")))
        }

        gameRules.run {
            if (getGameLevelHighscore(levelEntity.level.levelId) == gameRules.DEFAULT_HIGHSCORE_VALUE) {
                setGameLevelHighscore(levelEntity.level.levelId, gameRules.SKIPPED_LEVEL_SCORE_VALUE)
            }
            HIGHEST_FINISHED_LEVEL = levelEntity.level.levelId
        }
        levelEntity.level.run {
            levelId++
            isSkippingLevel = true
        }

        val fadeOutDuration = .2f
        val fadeInDuration = .2f
        gameStage.addAction(
            Actions.sequence(
                Actions.run {
                    levelEntity.level.isChangingLevel = true
                    eventQueue.add(FadeOutEvent(fadeOutDuration))
                },
                Actions.delay(fadeOutDuration),
                Actions.run {
                    playScreen.menuOverlay.shouldUpdateLevelLabel = true
                    levelEntity.level.run {
                        loadMap = true
                        forceUpdateMap = true
                    }
                    levelEntity.map.run {
                        eventQueue.add(UpdateRoundedPlatformsEvent())
                        forceCenterCameraOnPlayer = true
                    }
                },
                Actions.run { eventQueue.add(FadeInEvent(fadeInDuration)) },
                Actions.delay(fadeInDuration),
                Actions.run {
                    levelEntity.level.run {
                        isChangingLevel = false
                        isSkippingLevel = false
                        eventQueue.add(FlushPreferencesEvent())
                    }
                }
            )
        )
    }
}