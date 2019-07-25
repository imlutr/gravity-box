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
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

class PlaySpecificLevelEvent(val levelId: Int) : Event
class PlaySpecificLevelSystem(context: Context) : EventSystem<PlaySpecificLevelEvent>(context.inject(), PlaySpecificLevelEvent::class) {
    // Injected objects
    private val gameStage: GameStage = context.inject()
    private val playScreen: PlayScreen = context.inject()
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEvent(event: PlaySpecificLevelEvent, deltaTime: Float) {
        playSpecificLevel(event.levelId)
    }

    private fun playSpecificLevel(specificLevelId: Int) {
        gameStage.addAction(
            Actions.sequence(
                Actions.fadeOut(.25f, Interpolation.pow3In),
                Actions.run {
                    levelEntity.level.run {
                        levelId = specificLevelId
                        isLevelFinished = false
                        isRestarting = true
                        loadMap = true
                        forceUpdateMap = true
                    }
                    levelEntity.map.run {
                        forceCenterCameraOnPlayer = true
                        rank = -1
                        resetPassengers()
                    }
                    playScreen.run {
                        finishOverlay.hide()
                        menuOverlay.shouldUpdateLevelLabel = true
                    }
                },
                Actions.fadeIn(.25f, Interpolation.pow3In),
                Actions.run {
                    levelEntity.level.isRestarting = false
                }
            )
        )

    }
}