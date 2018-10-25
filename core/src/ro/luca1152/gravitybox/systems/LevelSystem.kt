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

package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import ktx.actors.minus
import ktx.collections.GdxArray
import ro.luca1152.gravitybox.components.image
import ro.luca1152.gravitybox.entities.BulletEntity
import ro.luca1152.gravitybox.entities.MapEntity
import ro.luca1152.gravitybox.entities.PlayerEntity
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.utils.GameStage
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Handles every event related to levels, such as restarting the level.
 */
class LevelSystem(gameEventSignal: Signal<GameEvent> = Injekt.get(),
                  private val playerEntity: PlayerEntity = Injekt.get(),
                  private val mapEntity: MapEntity = Injekt.get(),
                  private val world: World = Injekt.get(),
                  private val stage: GameStage = Injekt.get()) : EntitySystem() {
    private val eventQueue = EventQueue()

    init {
        gameEventSignal.add(eventQueue)
    }

    override fun update(deltaTime: Float) {
        eventQueue.getEvents().forEach { event ->
            if (event == GameEvent.LEVEL_RESTART) restartLevel()
            if (event == GameEvent.LEVEL_FINISHED) nextLevel()
        }
    }

    private val bodies = GdxArray<Body>()
    private fun restartLevel() {
        fun removeBullets() {
            world.getBodies(bodies)
            bodies.forEach { body ->
                if (body.userData is BulletEntity) {
                    stage - (body.userData as BulletEntity).image
                    world.destroyBody(body)
                }
            }
        }

        playerEntity.reset()
        removeBullets()
    }

    private fun nextLevel() {
        fun removeAllBodies() {
            world.getBodies(bodies)
            bodies.forEach { body -> world.destroyBody(body) }
        }

        removeAllBodies()
        mapEntity.loadMap(mapEntity.levelNumber + 1)
    }
}