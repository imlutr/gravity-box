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

package ro.luca1152.gravitybox.listeners

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.signals.Signal
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.components.CollisionBoxComponent
import ro.luca1152.gravitybox.components.FinishComponent
import ro.luca1152.gravitybox.components.PlayerComponent
import ro.luca1152.gravitybox.components.collisionBox
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.utils.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class CollisionBoxListener(private val gameEventSignal: Signal<GameEvent> = Injekt.get()) : EntitySystem() {
    var entities: ImmutableArray<Entity> = ImmutableArray(Array.of(Entity::class.java))

    override fun addedToEngine(engine: Engine) {
        entities = engine.getEntitiesFor(Family.all(CollisionBoxComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        // Find two boxes that collide one with each other
        for (i in 0 until entities.size() - 1)
            for (j in i + 1 until entities.size()) {
                val overlaps = entities[i].collisionBox.box.overlaps(entities[j].collisionBox.box)

                // Find the specific entities
                val playerEntity = findEntity(PlayerComponent, entities[i], entities[j])
                val finishEntity = findEntity(FinishComponent, entities[i], entities[j])

                // The player collided with the finish point
                if (finishEntity != null && playerEntity != null)
                    ColorScheme.useDarkColorScheme = overlaps
            }
    }
}