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

import com.badlogic.ashley.core.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import ktx.actors.plus
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.metersToPixels
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Button
import ro.luca1152.gravitybox.utils.ui.ClickButton
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Positions the overlay over the selected platform. */
class OverlayPositioningSystem(skin: Skin = Injekt.get(),
                               private val gameStage: GameStage = Injekt.get(),
                               private val gameCamera: GameCamera = Injekt.get(),
                               private val overlayCamera: OverlayCamera = Injekt.get(),
                               private val overlayStage: OverlayStage = Injekt.get(),
                               private val engine: PooledEngine = Injekt.get()) : EntitySystem() {
    private val leftArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-left-arrow-icon")
        iconCell!!.padLeft(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : ClickListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            var initialImageWidth = 0f
            var initialImageHeight = 0f
            var initialImageX = 0f
            var initialImageY = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                image.img.run {
                    initialImageWidth = width
                    initialImageHeight = height
                    initialImageX = this.x
                    initialImageY = this.y
                }
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                scaleMapObject(x, y, this@apply, selectedMapObject!!, toLeft = true)
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                undoRedoEntity.undoRedo.addExecutedCommand(ResizeCommand(selectedMapObject!!,
                        image.width - initialImageWidth, image.height - initialImageHeight,
                        image.img.x - initialImageX, image.img.y - initialImageY))
            }
        })
    }
    private val rightArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-right-arrow-icon")
        iconCell!!.padRight(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : ClickListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            var initialImageWidth = 0f
            var initialImageHeight = 0f
            var initialImageX = 0f
            var initialImageY = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                image.img.run {
                    initialImageWidth = width
                    initialImageHeight = height
                    initialImageX = this.x
                    initialImageY = this.y
                }
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                scaleMapObject(x, y, this@apply, selectedMapObject!!, toRight = true)
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                undoRedoEntity.undoRedo.addExecutedCommand(ResizeCommand(selectedMapObject!!,
                        image.width - initialImageWidth, image.height - initialImageHeight,
                        image.img.x - initialImageX, image.img.y - initialImageY))
            }
        })
    }
    private val deleteButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-x-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            val deleteCommand = DeleteCommand(selectedMapObject!!)
            deleteCommand.execute()
            undoRedoEntity.undoRedo.addExecutedCommand(deleteCommand)
        })
    }
    private val rotateButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-rotate-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : DragListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            var initialImageRotation = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                rotationLabel.isVisible = true
                initialImageRotation = image.img.rotation
                updateRotationLabel()
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)

                val mouseCoords = screenToWorldCoordinates(Gdx.input.x, Gdx.input.y)
                var newRotation = toPositiveAngle(MathUtils.atan2(mouseCoords.y - image.y, mouseCoords.x - image.x) * MathUtils.radiansToDegrees)

                // The rotate button is not on the same Ox axis as the map object, which in turn affects the rotation
                val deltaAngle = getAngleBetween(this@apply, image)

                newRotation -= deltaAngle
                newRotation = MathUtils.round(newRotation).toFloat()
                newRotation = newRotation.roundToNearest(45f, 5f)
                newRotation = toPositiveAngle(newRotation)

                image.img.rotation = newRotation
                overlayLevel1.rotation = newRotation
                this@apply.icon!!.rotation = 360f - newRotation

                updateRotationLabel()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                rotationLabel.isVisible = false
                if (image.img.rotation != initialImageRotation)
                    undoRedoEntity.undoRedo.addExecutedCommand(RotateCommand(selectedMapObject!!, image.img.rotation - initialImageRotation))
            }

            private fun getAngleBetween(rotateButton: Button, objectImage: ImageComponent): Float {
                val oldRotation = overlayLevel1.rotation
                overlayLevel1.rotation = 0f // Makes calculating the angle easier
                val buttonCoords = rotateButton.localToScreenCoordinates(Vector2(0f, 0f))
                val objectCenterCoords = objectImage.img.localToScreenCoordinates(Vector2(objectImage.width / 2f, objectImage.height / 2f))
                overlayLevel1.rotation = oldRotation
                return Math.abs(MathUtils.atan2(buttonCoords.y - objectCenterCoords.y, buttonCoords.x - objectCenterCoords.x) * MathUtils.radiansToDegrees)
            }

            private fun updateRotationLabel() {
                rotationLabel.run {
                    setText("${overlayLevel1.rotation.toInt()}°")
                    val pos = this@apply.localToStageCoordinates(Vector2(this@apply.width / 2f, this@apply.height / 2f))
                    x = pos.x - prefWidth / 2f
                    y = pos.y + this@apply.height / 2f + buttonsPaddingY / 2f - height / 2f
                }
            }
        })
    }
    private val rotationLabel = Label("", skin, "bold-37", ColorScheme.darkerDarkColor).apply {
        isVisible = false
    }
    private val horizontalPositionButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-horizontal-arrow")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : DragListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            private var initialImageX = 0f
            private var initialMouseXInWorldCoords = 0f

            override fun dragStart(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.dragStart(event, x, y, pointer)
                initialImageX = image.x
                initialMouseXInWorldCoords = gameStage.screenToStageCoordinates(Vector2(Gdx.input.x.toFloat(), 0f)).x
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                if (!isDragging) return // Make sure dragStart() is called first

                val mouseXInWorldCoords = gameStage.screenToStageCoordinates(Vector2(Gdx.input.x.toFloat(), 0f)).x
                image.x = initialImageX + (mouseXInWorldCoords - initialMouseXInWorldCoords)
                image.x = image.x.roundToNearest(.5f, .15f)

                repositionOverlay()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                if (image.x != initialImageX)
                    undoRedoEntity.undoRedo.addExecutedCommand(MoveCommand(selectedMapObject!!, image.x - initialImageX, 0f))
            }
        })
    }
    private val verticalPositionButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-vertical-arrow")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : DragListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            private var initialImageY = 0f
            private var initialMouseYInWorldCoords = 0f

            override fun dragStart(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.dragStart(event, x, y, pointer)
                initialImageY = image.y
                initialMouseYInWorldCoords = gameStage.screenToStageCoordinates(Vector2(0f, Gdx.input.y.toFloat())).y
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                if (!isDragging) return // Make sure dragStart() is called first

                val mouseYInWorldCoords = gameStage.screenToStageCoordinates(Vector2(0f, Gdx.input.y.toFloat())).y
                image.y = initialImageY + (mouseYInWorldCoords - initialMouseYInWorldCoords)
                image.y = image.y.roundToNearest(.5f, .15f, 0f)

                repositionOverlay()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                if (image.y != initialImageY)
                    undoRedoEntity.undoRedo.addExecutedCommand(MoveCommand(selectedMapObject!!, 0f, image.y - initialImageY))
            }
        })
    }

    private val selectedMapObject: Entity?
        get() = getSelectedObject()
    private val undoRedoEntity: Entity = engine.getEntitiesFor(Family.all(UndoRedoComponent::class.java).get()).first()
    private val selectedMapObjectPolygon = Polygon().apply { vertices = FloatArray(8) }
    private val labels = Group().apply { this + rotationLabel }
    private val overlayLevel1 = Group().apply { this + horizontalPositionButton + verticalPositionButton + rotateButton }
    private val overlayLevel2 = Group().apply { this + leftArrowButton + rightArrowButton + deleteButton }
    private val buttonsPaddingX = 20f
    private val buttonsPaddingY = 50f

    override fun addedToEngine(engine: Engine?) {
        overlayStage.run {
            addActor(labels)
            addActor(overlayLevel1)
            addActor(overlayLevel2)
        }
    }

    override fun update(deltaTime: Float) {
        if (selectedMapObject == null) {
            hideOverlay()
        } else {
            showOverlay()
            updateOverlayShown()
            updateOverlaySize()
            repositionButtons()
            repositionOverlay()
            updateButtonsVisibility()
        }
    }

    override fun removedFromEngine(engine: Engine?) {
        labels.remove()
        overlayLevel1.remove()
        overlayLevel2.remove()
    }

    private fun hideOverlay() {
        overlayLevel1.isVisible = false
        overlayLevel2.isVisible = false
        labels.isVisible = false
    }

    private fun showOverlay() {
        overlayLevel1.isVisible = true
        overlayLevel2.isVisible = true
        labels.isVisible = true
    }

    private fun scaleMapObject(xDragged: Float, yDragged: Float, buttonDragged: Button, linkedMapObject: Entity,
                               toLeft: Boolean = false, toRight: Boolean = false) {
        if (!toLeft && !toRight)
            error { "No scale direction given." }
        if (toLeft && toRight)
            error { "Can't scale in two directions." }

        // Translate [xDragged] to game coords system and subtract half of the button's width so deltaX is 0 in the button's center
        var deltaX = (xDragged.pixelsToMeters - (buttonDragged.width / 2f).pixelsToMeters) * gameCamera.zoom
        if (toRight) deltaX = -deltaX

        val image = linkedMapObject.image
        var newWidth = Math.max(.5f, image.width - deltaX)
        newWidth = newWidth.roundToNearest(.5f, .15f)

        // Scale the platform correctly, taking in consideration its rotation and the scaling direction
        val localLeft = if (toLeft) -(newWidth - image.width) else 0f
        val localRight = if (toRight) (newWidth - image.width) else 0f
        updateObjectPolygon(image.img.x, image.img.y, image.width, image.height, image.img.rotation, localLeft, localRight)
        val position = selectedMapObjectPolygon.getRectangleCenter()
        image.width = newWidth
        image.setPosition(position.x, position.y)

        // In case a listener's touchDragged calls this function after this system is done updating, then these functions
        // wouldn't get called, which would result in a slight jitter movement
        repositionButtons()
        updateOverlaySize()
        repositionOverlay()
    }

    private fun updateObjectPolygon(x: Float, y: Float, width: Float, height: Float, rotationInDegrees: Float = 0f, localLeft: Float = 0f, localRight: Float = 0f) {
        selectedMapObjectPolygon.run {
            rotation = 0f
            vertices.run {
                set(0, localLeft); set(1, 0f) // bottom left corner
                set(2, width + localRight); set(3, 0f) // bottom right corner
                set(4, width + localRight); set(5, height) // top right corner
                set(6, localLeft); set(7, height) // top left corner
            }
            setPosition(x, y)
            setOrigin(width / 2f, height / 2f)
            rotation = rotationInDegrees
        }
    }



    private fun getSelectedObject(): Entity? {
        val selectedObjects = engine.getEntitiesFor(Family.all(SelectedObjectComponent::class.java).get())
        check(selectedObjects.size() <= 1) { "There can't be more than one selected object." }

        return when {
            selectedObjects.size() == 0 -> null
            else -> selectedObjects.first()
        }
    }

    private fun repositionButtons() {
        val image = selectedMapObject!!.image
        leftArrowButton.setPosition(0f, 0f)
        rightArrowButton.setPosition(leftArrowButton.width + buttonsPaddingX + image.width.metersToPixels / gameCamera.zoom + buttonsPaddingX, 0f)
        deleteButton.run {
            setPosition(rightArrowButton.x, deleteButton.height + buttonsPaddingY)
            icon!!.rotation = 360f - image.img.rotation
        }
        rotateButton.setPosition(rightArrowButton.x, rightArrowButton.y + rightArrowButton.height + buttonsPaddingY)
        horizontalPositionButton.run {
            setPosition(overlayLevel1.width / 2f - horizontalPositionButton.width / 2f,
                    -height / 2f - image.height.metersToPixels / 2f / gameCamera.zoom - buttonsPaddingX)
            icon!!.rotation = 360f - image.img.rotation
        }
        verticalPositionButton.run {
            setPosition(rightArrowButton.x, rightArrowButton.y)
            icon!!.rotation = 360f - image.img.rotation
        }
    }

    private fun updateOverlaySize() {
        val image = selectedMapObject!!.image
        overlayLevel2.run {
            width = leftArrowButton.width + buttonsPaddingX + (image.width.metersToPixels / gameCamera.zoom) + buttonsPaddingX + rightArrowButton.width
            height = rightArrowButton.height + buttonsPaddingY + rotateButton.height
            setOrigin(width / 2f, leftArrowButton.height / 2f)
            rotation = image.img.rotation
        }
        overlayLevel1.run {
            setSize(overlayLevel2.width, overlayLevel2.height)
            setOrigin(overlayLevel2.originX, overlayLevel2.originY)
            rotation = overlayLevel2.rotation
        }
    }

    private fun repositionOverlay() {
        val image = selectedMapObject!!.image
        val objectCoords = worldToOverlayCameraCoordinates(image.x, image.y)
        overlayLevel2.run {
            x = objectCoords.x - overlayLevel2.width / 2f
            y = objectCoords.y - leftArrowButton.height / 2f
        }
        overlayLevel1.setPosition(overlayLevel2.x, overlayLevel2.y)
    }

    private fun updateButtonsVisibility() {
        selectedMapObject!!.mapObjectOverlay.run {
            verticalPositionButton.isVisible = showMovementButtons
            horizontalPositionButton.isVisible = showMovementButtons
            rotateButton.isVisible = showRotationButton
            rightArrowButton.isVisible = showResizingButtons
            leftArrowButton.isVisible = showResizingButtons
            deleteButton.isVisible = showDeletionButton
        }
    }

    private fun updateOverlayShown() {
        val level = (selectedMapObject as Entity).selectedObject.level
        val showLevel2Overlay = selectedMapObject!!.mapObjectOverlay.showResizingButtons || selectedMapObject!!.mapObjectOverlay.showDeletionButton
        overlayLevel1.isVisible = if (showLevel2Overlay) (level == 1) else true
        overlayLevel2.isVisible = if (showLevel2Overlay) (level == 2) else false
    }

    private fun worldToOverlayCameraCoordinates(x: Float, y: Float): Vector3 {
        val coords = Vector3(x, y, 0f)

        // [coords] are now in screen coordinates
        gameCamera.project(coords)

        // When you unproject coordinates, the (0;0) is in the top left corner
        coords.y = Gdx.graphics.height - coords.y

        // [coords] are now in overlayCamera coordinates
        overlayCamera.unproject(coords)

        return coords
    }

    private fun toPositiveAngle(angle: Float): Float {
        var newAngle = angle % 360f
        if (newAngle < 0f) newAngle += 360f
        return newAngle
    }
}