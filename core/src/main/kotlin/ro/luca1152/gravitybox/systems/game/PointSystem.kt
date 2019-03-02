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
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import ro.luca1152.gravitybox.components.BodyComponent
import ro.luca1152.gravitybox.components.MapComponent
import ro.luca1152.gravitybox.components.PointComponent
import ro.luca1152.gravitybox.components.point
import ro.luca1152.gravitybox.components.utils.removeAndResetEntity

/** Removes points once they are collected. */
class PointSystem(private val map: MapComponent) :
    IteratingSystem(Family.all(PointComponent::class.java, BodyComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.point.isCollected) {
            map.collectedPoints++
            engine.removeAndResetEntity(entity)
        }
    }
}