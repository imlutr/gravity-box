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

package ro.luca1152.gravitybox.systems.editor

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.components.editor.SelectedObjectComponent
import ro.luca1152.gravitybox.components.editor.SnapComponent
import ro.luca1152.gravitybox.components.editor.SnapComponent.Companion.DRAG_SNAP_THRESHOLD
import ro.luca1152.gravitybox.components.editor.SnapComponent.Companion.ROTATION_SNAP_THRESHOLD
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.editor.snap
import ro.luca1152.gravitybox.components.game.ImageComponent
import ro.luca1152.gravitybox.components.game.image
import ro.luca1152.gravitybox.components.game.polygon
import ro.luca1152.gravitybox.utils.kotlin.getNullableSingletonFor

class ObjectSnappingSystem : EntitySystem() {
    private val selectedObject: Entity?
        get() = engine.getNullableSingletonFor(Family.all(SelectedObjectComponent::class.java).get())
    private val onScreenObjects = Array<Entity>()

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        updateOnScreenObjects()
        snapSelectedObject()
    }

    private fun updateOnScreenObjects() {
        onScreenObjects.clear()
        engine.getEntitiesFor(Family.all(ImageComponent::class.java, SnapComponent::class.java).get()).forEach {
            if (it.image.img.isOnScreen()) {
                onScreenObjects.add(it)
            }
        }
    }

    private fun Actor.isOnScreen(): Boolean {
        val bottomLeftScreenPos = localToScreenCoordinates(Vector2(0f, 0f))
        val topRightScreenPos = localToScreenCoordinates(Vector2(width, height))
        val bottomLeftIsOnScreen = bottomLeftScreenPos.x >= 0f && bottomLeftScreenPos.x <= Gdx.graphics.width &&
                bottomLeftScreenPos.y >= 0f && bottomLeftScreenPos.y <= Gdx.graphics.height
        val topRightIsOnScreen = topRightScreenPos.x >= 0f && topRightScreenPos.x <= Gdx.graphics.width
                && topRightScreenPos.y >= 0f && topRightScreenPos.y <= Gdx.graphics.height
        return bottomLeftIsOnScreen || topRightIsOnScreen
    }

    private fun snapSelectedObject() {
        if (selectedObject == null || onScreenObjects.isEmpty) {
            return
        }
        snapObjectPosition()
        snapObjectRotation()
    }

    private fun snapObjectPosition() {
        if (!selectedObject!!.editorObject.isDragging) {
            return
        }
        snapObjectX()
        snapObjectY()
    }

    private fun snapObjectX() {
        if (!selectedObject!!.editorObject.isDraggingHorizontally) {
            return
        }
        onScreenObjects.forEach {
            if (it != selectedObject) {
                val diffRightLeft = it.polygon.rightmostX - selectedObject!!.polygon.leftmostX
                val diffLeftLeft = it.polygon.leftmostX - selectedObject!!.polygon.leftmostX
                val diffRightRight = it.polygon.rightmostX - selectedObject!!.polygon.rightmostX
                val diffLeftRight = it.polygon.leftmostX - selectedObject!!.polygon.rightmostX
                when {
                    Math.abs(diffRightLeft) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerX += diffRightLeft
                        return
                    }
                    Math.abs(diffLeftLeft) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerX += diffLeftLeft
                        return
                    }
                    Math.abs(diffRightRight) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerX += diffRightRight
                        return
                    }
                    Math.abs(diffLeftRight) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerX += diffLeftRight
                        return
                    }
                }
            }
        }
    }

    private fun snapObjectY() {
        if (!selectedObject!!.editorObject.isDraggingVertically) {
            return
        }
        onScreenObjects.forEach {
            if (it != selectedObject) {
                val diffTopBottom = it.polygon.topmostY - selectedObject!!.polygon.bottommostY
                val diffBottomBottom = it.polygon.bottommostY - selectedObject!!.polygon.bottommostY
                val diffTopTop = it.polygon.topmostY - selectedObject!!.polygon.topmostY
                val diffBottomTop = it.polygon.bottommostY - selectedObject!!.polygon.topmostY
                when {
                    Math.abs(diffTopBottom) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerY += diffTopBottom
                        return
                    }
                    Math.abs(diffBottomBottom) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerY += diffBottomBottom
                        return
                    }
                    Math.abs(diffTopTop) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerY += diffTopTop
                        return
                    }
                    Math.abs(diffBottomTop) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerY += diffBottomTop
                        return
                    }
                }
            }
        }
    }

    private fun snapObjectRotation() {
        if (!selectedObject!!.editorObject.isRotating) {
            return
        }
        onScreenObjects.forEach {
            if (it != selectedObject) {
                val rotationDiff = it.polygon.polygon.rotation - selectedObject!!.polygon.polygon.rotation
                if (Math.abs(rotationDiff) < ROTATION_SNAP_THRESHOLD) {
                    selectedObject!!.image.img.rotation += rotationDiff
                    selectedObject!!.snap.snapRotationAngle = it.polygon.polygon.rotation
                    return
                }
            }
        }
        selectedObject!!.snap.resetSnappedRotation()
    }
}