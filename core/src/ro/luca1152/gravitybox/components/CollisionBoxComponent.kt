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

package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Rectangle

/**
 * Used to detect when the player is in the finish point.
 * Box2D collisions can't be used because the player doesn't collide with the finish point.
 */
class CollisionBoxComponent(size: Float) : Component {
    val box = Rectangle().apply {
        setSize(size)
    }

    companion object : ComponentResolver<CollisionBoxComponent>(CollisionBoxComponent::class.java)
}

val Entity.collisionBox
    get() = CollisionBoxComponent[this]