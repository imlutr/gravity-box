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

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.SnapComponent
import ro.luca1152.gravitybox.components.editor.SnapComponent.Companion.DRAG_SNAP_THRESHOLD
import ro.luca1152.gravitybox.components.editor.SnapComponent.Companion.RESIZE_SNAP_THRESHOLD
import ro.luca1152.gravitybox.components.editor.SnapComponent.Companion.ROTATION_SNAP_THRESHOLD
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.editor.snap
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.filterNullableSingleton
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

/** Snaps nearby map objects together when moved. */
class ObjectSnappingSystem : EntitySystem() {
    private lateinit var levelEntity: Entity
    private val selectedObject: Entity?
        get() = engine.getEntitiesFor(Family.all(EditorObjectComponent::class.java).get()).filterNullableSingleton { it.editorObject.isSelected }
    private val onScreenObjects = Array<Entity>()
    private var didSnapPlatform = false

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        didSnapPlatform = false
        updateOnScreenObjects()
        snapSelectedObject()
        updateRoundedPlatforms()
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
        snapObjectSize()
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
                        didSnapPlatform = true
                        return
                    }
                    Math.abs(diffLeftLeft) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerX += diffLeftLeft
                        didSnapPlatform = true
                        return
                    }
                    Math.abs(diffRightRight) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerX += diffRightRight
                        didSnapPlatform = true
                        return
                    }
                    Math.abs(diffLeftRight) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerX += diffLeftRight
                        didSnapPlatform = true
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
                        didSnapPlatform = true
                        return
                    }
                    Math.abs(diffBottomBottom) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerY += diffBottomBottom
                        didSnapPlatform = true
                        return
                    }
                    Math.abs(diffTopTop) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerY += diffTopTop
                        didSnapPlatform = true
                        return
                    }
                    Math.abs(diffBottomTop) <= DRAG_SNAP_THRESHOLD -> {
                        selectedObject!!.image.centerY += diffBottomTop
                        didSnapPlatform = true
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
                    didSnapPlatform = true
                    return
                }
            }
        }
        selectedObject!!.snap.resetSnappedRotation()
    }

    private fun snapObjectSize() {
        selectedObject!!.polygon.expandPolygonWith()
        if (!selectedObject!!.editorObject.isResizing) {
            return
        }
        snapObjectLeft()
        snapObjectRight()
        snapObjectTop()
        snapObjectBottom()
    }

    private fun snapObjectLeft() {
        if (!selectedObject!!.editorObject.isResizingLeftwards) {
            return
        }
        onScreenObjects.forEach {
            if (it != selectedObject) {
                val diffLeftLeft = it.polygon.leftmostX - selectedObject!!.polygon.leftmostX
                val diffRightLeft = it.polygon.rightmostX - selectedObject!!.polygon.leftmostX
                when {
                    Math.abs(diffLeftLeft) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(left = diffLeftLeft)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapLeft = selectedObject!!.polygon.leftmostX
                            didSnapPlatform = true
                        }
                        return
                    }
                    Math.abs(diffRightLeft) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(left = diffRightLeft)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapLeft = selectedObject!!.polygon.leftmostX
                            didSnapPlatform = true
                        }
                        return
                    }
                }
            }
        }
        selectedObject!!.snap.resetSnappedLeft()
    }

    private fun snapObjectRight() {
        if (!selectedObject!!.editorObject.isResizingRightwards) {
            return
        }
        onScreenObjects.forEach {
            if (it != selectedObject) {
                val diffLeftRight = it.polygon.leftmostX - selectedObject!!.polygon.rightmostX
                val diffRightRight = it.polygon.rightmostX - selectedObject!!.polygon.rightmostX
                when {
                    Math.abs(diffLeftRight) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(right = diffLeftRight)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapRight = selectedObject!!.polygon.rightmostX
                            didSnapPlatform = true
                        }
                        return
                    }
                    Math.abs(diffRightRight) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(right = diffRightRight)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapRight = selectedObject!!.polygon.rightmostX
                            didSnapPlatform = true
                        }
                        return
                    }
                }
            }
        }
        selectedObject!!.snap.resetSnappedRight()
    }

    private fun snapObjectTop() {
        if (!selectedObject!!.editorObject.isResizingUpwards) {
            return
        }
        onScreenObjects.forEach {
            if (it != selectedObject) {
                val diffBottomTop = it.polygon.bottommostY - selectedObject!!.polygon.topmostY
                val diffTopTop = it.polygon.topmostY - selectedObject!!.polygon.topmostY
                when {
                    Math.abs(diffBottomTop) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(top = diffBottomTop)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapTop = selectedObject!!.polygon.topmostY
                            didSnapPlatform = true
                        }
                        return
                    }
                    Math.abs(diffTopTop) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(top = diffTopTop)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapTop = selectedObject!!.polygon.topmostY
                            didSnapPlatform = true
                        }
                        return
                    }
                }
            }
        }
        selectedObject!!.snap.resetSnappedTop()
    }

    private fun snapObjectBottom() {
        if (!selectedObject!!.editorObject.isResizingDownwards) {
            return
        }
        onScreenObjects.forEach {
            if (it != selectedObject) {
                val diffBottomBottom = it.polygon.bottommostY - selectedObject!!.polygon.bottommostY
                val diffTopBottom = it.polygon.topmostY - selectedObject!!.polygon.bottommostY
                when {
                    Math.abs(diffTopBottom) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(bottom = diffTopBottom)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapBottom = selectedObject!!.polygon.bottommostY
                            didSnapPlatform = true
                        }
                        return
                    }
                    Math.abs(diffBottomBottom) <= RESIZE_SNAP_THRESHOLD -> {
                        selectedObject!!.run {
                            polygon.expandPolygonWith(bottom = diffBottomBottom)
                            image.updateFromPolygon(polygon.polygon)
                            snap.snapBottom = selectedObject!!.polygon.bottommostY
                            didSnapPlatform = true
                        }
                        return
                    }
                }
            }
        }
        selectedObject!!.snap.resetSnappedBottom()
    }

    private fun updateRoundedPlatforms() {
        if (didSnapPlatform) {
            levelEntity.map.updateRoundedPlatforms = true
            selectedObject!!.polygon.update()
        }
    }
}