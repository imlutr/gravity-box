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
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.utils.components.ComponentResolver

class PolygonComponent : Component, Poolable {
    private var linkedImage = Image()
    private var polygon = Polygon()
    var leftmostX = Float.MAX_VALUE
    var rightmostX = Float.MIN_VALUE
    var bottommostY = Float.MAX_VALUE
    var topmostY = Float.MIN_VALUE

    fun set(linkedImage: Image) {
        this.linkedImage = linkedImage
    }

    fun update() {
        updatePolygon()
        updateBounds()
    }

    private fun updatePolygon() {
        updateVertices()
        polygon.run {
            setPosition(linkedImage.x, linkedImage.y)
            setOrigin(linkedImage.originX, linkedImage.originY)
            rotation = linkedImage.rotation
        }
    }

    private fun updateVertices() {
        polygon.vertices = floatArrayOf(
            0f, 0f,
            linkedImage.width, 0f,
            linkedImage.width, linkedImage.height,
            0f, linkedImage.height
        )
    }

    private fun updateBounds() {
        resetBounds()
        val vertices = polygon.transformedVertices
        for (i in 0 until vertices.size step 2) {
            leftmostX = Math.min(leftmostX, vertices[i])
            rightmostX = Math.max(rightmostX, vertices[i])
        }
        for (i in 1 until vertices.size step 2) {
            bottommostY = Math.min(bottommostY, vertices[i])
            topmostY = Math.max(topmostY, vertices[i])
        }
    }

    private fun resetBounds() {
        leftmostX = Float.MAX_VALUE
        rightmostX = Float.MIN_VALUE
        bottommostY = Float.MAX_VALUE
        topmostY = Float.MIN_VALUE
    }

    override fun reset() {
        polygon = Polygon()
        linkedImage = Image()
        resetBounds()
    }

    companion object : ComponentResolver<PolygonComponent>(PolygonComponent::class.java)
}

val Entity.polygon: PolygonComponent
    get() = PolygonComponent[this]