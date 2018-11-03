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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.signals.Signal
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.physics.box2d.World
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.GameEvent
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PointSystem(private val map: MapComponent,
                  gameEventSignal: Signal<GameEvent> = Injekt.get(),
                  private val world: World = Injekt.get()) : IteratingSystem(Family.all(CollectibleComponent::class.java, PhysicsComponent::class.java).get()) {
    private val eventQueue = EventQueue()

    init {
        gameEventSignal.add(eventQueue)
    }

    override fun update(deltaTime: Float) {
        if (eventQueue.getEvents().contains(GameEvent.PLAYER_COLLECTED_POINT))
            super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.collectible.isCollected) {
            map.collectedCollectibles++
            world.destroyBody(entity.physics.body)
            engine.removeEntity(entity)
        }
    }
}