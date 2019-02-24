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
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.signals.Signal
import ro.luca1152.gravitybox.components.LevelComponent
import ro.luca1152.gravitybox.components.PlayerComponent
import ro.luca1152.gravitybox.components.body
import ro.luca1152.gravitybox.components.level
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Marks the level as to be restarted when the player is off-screen. */
class LevelAutoRestartSystem(private val gameEventSignal: Signal<GameEvent> = Injekt.get()) : EntitySystem() {
    private lateinit var playerEntity: Entity
    private lateinit var levelEntity: Entity
    private val playerIsOffScreen
        get() = playerEntity.body.body.worldCenter.y < -10f

    override fun addedToEngine(engine: Engine) {
        playerEntity = engine.getSingletonFor(Family.all(PlayerComponent::class.java).get())
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        if (playerIsOffScreen) {
            gameEventSignal.dispatch(GameEvent.LEVEL_RESTART) // TODO: Remove signals
            levelEntity.level.restartLevel = true
        }
    }
}