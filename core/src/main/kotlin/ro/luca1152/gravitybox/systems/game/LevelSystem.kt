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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.signals.Signal
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.removeAndResetEntity
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.utils.box2d.MapBodyBuilder
import ro.luca1152.gravitybox.utils.kotlin.approxEqualTo
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Handles every event related to levels, such as restarting the level. */
class LevelSystem(private var mapEntity: Entity,
                  private val finishEntity: Entity,
                  private val playerEntity: Entity,
                  gameEventSignal: Signal<GameEvent> = Injekt.get()) : EntitySystem() {
    private val eventQueue = EventQueue()

    init {
        gameEventSignal.add(eventQueue)
    }

    override fun update(deltaTime: Float) {
        eventQueue.getEvents().forEach { event ->
            if (event == GameEvent.LEVEL_RESTART) restartLevel()
        }

        if (levelFinished())
            nextLevel()
    }

    private fun levelFinished(): Boolean {
        return mapEntity.map.isFinished && ColorScheme.useDarkColorScheme && ColorScheme.currentDarkColor.approxEqualTo(ColorScheme.currentDarkLerpColor)
    }

    private fun restartLevel() {
        playerEntity.player.reset(playerEntity.physics.body)
        for (entity in engine.getEntitiesFor(Family.one(PhysicsComponent::class.java, PlatformComponent::class.java)
                .exclude(PlayerComponent::class.java, FinishComponent::class.java).get()))
            engine.removeAndResetEntity(entity)
        mapEntity.map.set(mapEntity.map.levelNumber)
    }

    private fun nextLevel() {
        mapEntity.map.levelNumber++
        restartLevel()
        resetEntities()
    }

    private fun resetEntities() {
        playerEntity.physics.run {
            reset()
            set(MapBodyBuilder.buildPlayerBody(mapEntity.map.tiledMap), playerEntity)
        }
        finishEntity.physics.run {
            reset()
            set(MapBodyBuilder.buildFinishBody(mapEntity.map.tiledMap), finishEntity)
        }
    }
}