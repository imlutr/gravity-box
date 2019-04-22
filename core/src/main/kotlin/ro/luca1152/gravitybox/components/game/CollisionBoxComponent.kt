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

package ro.luca1152.gravitybox.components.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Contains a [Rectangle]. */
class CollisionBoxComponent : Component, Poolable {
    val box = Rectangle()
    var width = 0f
    var height = 0f

    fun set(width: Float, height: Float) {
        this.width = width
        this.height = height
        box.setSize(width, height)
    }

    override fun reset() {
        box.set(0f, 0f, 0f, 0f)
        width = 0f
        height = 0f
    }

    companion object : ComponentResolver<CollisionBoxComponent>(CollisionBoxComponent::class.java)
}

val Entity.collisionBox: CollisionBoxComponent
    get() = CollisionBoxComponent[this]

fun Entity.collisionBox(
    context: Context,
    width: Float, height: Float
) = add(createComponent<CollisionBoxComponent>(context).apply {
    set(width, height)
})!!