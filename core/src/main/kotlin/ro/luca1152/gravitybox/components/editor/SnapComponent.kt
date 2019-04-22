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
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.game.pixelsToMeters
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Entities with this component will be snapped together when nearby. */
class SnapComponent : Component, Poolable {
    companion object : ComponentResolver<SnapComponent>(SnapComponent::class.java) {
        const val ROTATION_SNAP_THRESHOLD = 7f
        val DRAG_SNAP_THRESHOLD = 10.pixelsToMeters
        val RESIZE_SNAP_THRESHOLD = 15.pixelsToMeters
    }

    var snapRotationAngle = Float.POSITIVE_INFINITY
    val rotationIsSnapped
        get() = snapRotationAngle != Float.POSITIVE_INFINITY

    var snapLeft = Float.POSITIVE_INFINITY
    var snapRight = Float.POSITIVE_INFINITY
    var snapBottom = Float.POSITIVE_INFINITY
    var snapTop = Float.POSITIVE_INFINITY

    var snapCenterX = Float.POSITIVE_INFINITY
    var snapCenterY = Float.POSITIVE_INFINITY

    fun resetSnappedLeft() {
        snapLeft = Float.POSITIVE_INFINITY
    }

    fun resetSnappedRight() {
        snapRight = Float.POSITIVE_INFINITY
    }

    fun resetSnappedBottom() {
        snapBottom = Float.POSITIVE_INFINITY
    }

    fun resetSnappedTop() {
        snapTop = Float.POSITIVE_INFINITY
    }

    fun resetSnappedX() {
        snapCenterX = Float.POSITIVE_INFINITY
    }

    fun resetSnappedY() {
        snapCenterY = Float.POSITIVE_INFINITY
    }

    fun resetSnappedRotation() {
        snapRotationAngle = Float.POSITIVE_INFINITY
    }

    fun resetSnappedSize() {
        resetSnappedLeft()
        resetSnappedRight()
        resetSnappedBottom()
        resetSnappedTop()
    }

    private fun resetSnappedPosition() {
        resetSnappedX()
        resetSnappedY()
    }

    override fun reset() {
        resetSnappedSize()
        resetSnappedPosition()
        resetSnappedRotation()
    }
}

val Entity.snap: SnapComponent
    get() = SnapComponent[this]

fun Entity.snap(context: Context) =
    add(createComponent<SnapComponent>(context))!!