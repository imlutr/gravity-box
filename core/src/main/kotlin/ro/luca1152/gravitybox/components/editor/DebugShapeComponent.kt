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

package ro.luca1152.gravitybox.components.editor

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Contains a shape which is drawn in the DebugRenderingSystem. */
class DebugShapeComponent : Component, Poolable {
    var polygon: Polygon? = null
    var rectangle: Rectangle? = null
    var point: Vector2? = null

    fun set(polygon: Polygon) {
        this.polygon = polygon
    }

    fun set(rectangle: Rectangle) {
        this.rectangle = rectangle
    }

    fun set(point: Vector2) {
        this.point = point
    }

    override fun reset() {
        polygon = null
        rectangle = null
    }

    companion object : ComponentResolver<DebugShapeComponent>(DebugShapeComponent::class.java)
}

val Entity.debugShape: DebugShapeComponent
    get() = DebugShapeComponent[this]

fun Entity.debugShape(context: Context) =
    add(createComponent<DebugShapeComponent>(context))!!