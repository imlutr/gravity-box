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
import com.badlogic.ashley.utils.ImmutableArray
import ro.luca1152.gravitybox.components.game.*

class CollisionBoxListener : EntitySystem() {
    private lateinit var entities: ImmutableArray<Entity>

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
                val pointEntity = findEntity(PointComponent, entities[i], entities[j])

                // If the player collided with the finish point, change the color scheme
//                if (playerEntity != null && finishEntity != null) TODO!!
//                    ColorScheme.useDarkColorScheme = overlaps

                // entity[i] overlaps entity[j]
                if (overlaps) {
                    // The player collected a point
                    if (playerEntity != null && pointEntity != null) {
                        pointEntity.point.isCollected = true
                    }
                }
            }
    }
}