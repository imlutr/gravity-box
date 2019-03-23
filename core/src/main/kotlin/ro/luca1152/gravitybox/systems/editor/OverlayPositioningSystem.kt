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
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import ktx.actors.plus
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.components.editor.SnapComponent.Companion.DRAG_SNAP_THRESHOLD
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.button.Button
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Positions the overlay. */
class OverlayPositioningSystem(
    skin: Skin = Injekt.get(),
    private val gameStage: GameStage = Injekt.get(),
    private val gameCamera: GameCamera = Injekt.get(),
    private val overlayCamera: OverlayCamera = Injekt.get(),
    private val overlayStage: OverlayStage = Injekt.get(),
    private val engine: PooledEngine = Injekt.get()
) : EntitySystem() {
    private lateinit var mapEntity: Entity
    private val leftArrowButton: ClickButton = ClickButton(
        skin,
        "small-round-button"
    ).apply {
        addIcon("small-left-arrow-icon")
        iconCell!!.padLeft(-4f) // The icon doesn't LOOK centered
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addListener(object : ClickListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            var initialImageWidth = 0f
            var initialImageHeight = 0f
            var initialImageX = 0f
            var initialImageY = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
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
                scaleMapObject(x, this@apply, selectedMapObject!!, toLeft = true)
                mapEntity.map.updateRoundedPlatforms = true
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.resetResizingBooleans()
                selectedMapObject!!.snap.resetSnappedSize()
                if (image.width != initialImageWidth || image.height != initialImageHeight)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        ResizeCommand(
                            selectedMapObject!!,
                            image.width - initialImageWidth, image.height - initialImageHeight,
                            image.leftX - initialImageX, image.bottomY - initialImageY
                        )
                    )
            }
        })
    }
    private val rightArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-right-arrow-icon")
        iconCell!!.padRight(-4f) // The icon doesn't LOOK centered
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addListener(object : ClickListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            var initialImageWidth = 0f
            var initialImageHeight = 0f
            var initialImageX = 0f
            var initialImageY = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
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
                scaleMapObject(x, this@apply, selectedMapObject!!, toRight = true)
                mapEntity.map.updateRoundedPlatforms = true
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.resetResizingBooleans()
                selectedMapObject!!.snap.resetSnappedSize()
                if (image.width != initialImageWidth || image.height != initialImageHeight)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        ResizeCommand(
                            selectedMapObject!!,
                            image.width - initialImageWidth, image.height - initialImageHeight,
                            image.leftX - initialImageX, image.bottomY - initialImageY
                        )
                    )
            }
        })
    }
    private val deleteButton: ClickButton = ClickButton(
        skin,
        "small-round-button"
    ).apply {
        addIcon("small-x-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addClickRunnable(Runnable {
            val deleteCommand = DeleteCommand(selectedMapObject!!, mapEntity)
            deleteCommand.execute()
            undoRedoEntity.undoRedo.addExecutedCommand(deleteCommand)
        })
    }
    private val rotateButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-rotate-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addListener(object : DragListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            var initialImageRotation = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                rotationLabel.isVisible = true
                initialImageRotation = image.img.rotation
                updateRotationLabel()
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                mapEntity.map.updateRoundedPlatforms = true
                selectedMapObject!!.editorObject.isRotating = true

                val mouseCoords = screenToWorldCoordinates(Gdx.input.x, Gdx.input.y)
                var newRotation = toPositiveAngle(
                    MathUtils.atan2(
                        mouseCoords.y - image.centerY,
                        mouseCoords.x - image.centerX
                    ) * MathUtils.radiansToDegrees
                )

                // The rotate button is not on the same Ox axis as the map object, which in turn affects the rotation
                val deltaAngle = getAngleBetween(this@apply, image)

                newRotation -= deltaAngle
                newRotation = MathUtils.round(newRotation).toFloat()
                newRotation = toPositiveAngle(newRotation)
                newRotation =
                    if (selectedMapObject!!.snap.rotationIsSnapped && Math.abs(newRotation - selectedMapObject!!.snap.snapRotationAngle) <= SnapComponent.ROTATION_SNAP_THRESHOLD) {
                        selectedMapObject!!.snap.snapRotationAngle
                    } else {
                        newRotation.roundToNearest(45f, 7f)
                    }
                newRotation = toPositiveAngle(newRotation)

                image.img.rotation = newRotation
                overlayLevel1.rotation = newRotation
                this@apply.icon!!.rotation = 360f - newRotation

                updateRotationLabel()
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.isRotating = false
                rotationLabel.isVisible = false
                if (image.img.rotation != initialImageRotation)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        RotateCommand(
                            selectedMapObject!!,
                            image.img.rotation - initialImageRotation
                        )
                    )
            }

            private fun getAngleBetween(rotateButton: Button, objectImage: ImageComponent): Float {
                val oldRotation = overlayLevel1.rotation
                overlayLevel1.rotation = 0f // Makes calculating the angle easier
                val buttonCoords = rotateButton.localToScreenCoordinates(Vector2(0f, 0f))
                val objectCenterCoords =
                    objectImage.img.localToScreenCoordinates(Vector2(objectImage.width / 2f, objectImage.height / 2f))
                overlayLevel1.rotation = oldRotation
                return Math.abs(
                    MathUtils.atan2(
                        buttonCoords.y - objectCenterCoords.y,
                        buttonCoords.x - objectCenterCoords.x
                    ) * MathUtils.radiansToDegrees
                )
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
    private val rotationLabel = DistanceFieldLabel("", skin, "bold", 37f, Colors.uiDownColor).apply {
        isVisible = false
    }
    private val horizontalPositionButton = ClickButton(
        skin,
        "small-round-button"
    ).apply {
        addIcon("small-horizontal-arrow")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addListener(object : DragListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            private var initialImageX = 0f
            private var initialMouseXInWorldCoords = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                initialImageX = image.centerX
                initialMouseXInWorldCoords = gameStage.screenToStageCoordinates(Vector2(Gdx.input.x.toFloat(), 0f)).x
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                if (!isDragging) return // Make sure dragStart() is called first
                mapEntity.map.updateRoundedPlatforms = true
                selectedMapObject!!.editorObject.isDraggingHorizontally = true

                val mouseXInWorldCoords = gameStage.screenToStageCoordinates(Vector2(Gdx.input.x.toFloat(), 0f)).x
                var newCenterX = initialImageX + (mouseXInWorldCoords - initialMouseXInWorldCoords)
                newCenterX = newCenterX.roundToNearest(.5f, .15f)

                if (Math.abs(newCenterX - selectedMapObject!!.snap.snapCenterX) <= DRAG_SNAP_THRESHOLD) {
                    return
                } else {
                    image.centerX = newCenterX
                }

                repositionOverlay()
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.isDraggingHorizontally = false
                selectedMapObject!!.snap.resetSnappedX()
                if (image.centerX != initialImageX)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        MoveCommand(
                            selectedMapObject!!,
                            image.centerX - initialImageX,
                            0f
                        )
                    )
            }
        })
    }
    private val verticalPositionButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-vertical-arrow")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addListener(object : DragListener() {
            private val image
                get() = (selectedMapObject as Entity).image
            private var initialImageY = 0f
            private var initialMouseYInWorldCoords = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                initialImageY = image.centerY
                initialMouseYInWorldCoords = gameStage.screenToStageCoordinates(Vector2(0f, Gdx.input.y.toFloat())).y
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                if (!isDragging) return // Make sure dragStart() is called first
                mapEntity.map.updateRoundedPlatforms = true
                selectedMapObject!!.editorObject.isDraggingVertically = true

                val mouseYInWorldCoords = gameStage.screenToStageCoordinates(Vector2(0f, Gdx.input.y.toFloat())).y
                var newCenterY = initialImageY + (mouseYInWorldCoords - initialMouseYInWorldCoords)
                if ((selectedMapObject as Entity).tryGet(PlatformComponent) != null) {
                    newCenterY = newCenterY.roundToNearest(1f, .125f, .5f)
                }

                if (Math.abs(selectedMapObject!!.snap.snapCenterY - newCenterY) <= DRAG_SNAP_THRESHOLD) {
                    return
                } else {
                    image.centerY = newCenterY
                }

                repositionOverlay()
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.isDraggingVertically = false
                selectedMapObject!!.snap.resetSnappedY()
                if (image.centerY != initialImageY)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        MoveCommand(
                            selectedMapObject!!,
                            0f,
                            image.centerY - initialImageY
                        )
                    )
            }
        })
    }

    private val selectedMapObject: Entity?
        get() = engine.getEntitiesFor(Family.all(EditorObjectComponent::class.java).get()).filterNullableSingleton { it.editorObject.isSelected }
    private val undoRedoEntity: Entity = engine.getEntitiesFor(Family.all(UndoRedoComponent::class.java).get()).first()
    private val selectedMapObjectPolygon = Polygon().apply { vertices = FloatArray(8) }
    private val labels = Group().apply { this + rotationLabel }
    private val overlayLevel1 =
        Group().apply { this + horizontalPositionButton + verticalPositionButton + rotateButton }
    private val overlayLevel2 = Group().apply { this + leftArrowButton + rightArrowButton + deleteButton }
    private val buttonsPaddingX = 20f
    private val buttonsPaddingY = 50f

    override fun addedToEngine(engine: Engine) {
        mapEntity = engine.getSingletonFor(Family.all(MapComponent::class.java).get())
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

    private fun scaleMapObject(
        xDragged: Float,
        buttonDragged: Button,
        linkedMapObject: Entity,
        toLeft: Boolean = false,
        toRight: Boolean = false
    ) {
        if (!toLeft && !toRight)
            error { "No scale direction given." }
        if (toLeft && toRight)
            error { "Can't scale in two directions." }

        // Translate [xDragged] to game coords system and subtract half of the button's width so deltaX is 0 in the button's center
        var deltaX = (xDragged.pixelsToMeters - (buttonDragged.width / 2f).pixelsToMeters) * gameCamera.zoom
        if (toRight) deltaX = -deltaX

        val image = linkedMapObject.image
        var newWidth = Math.max(.5f, image.width - deltaX)
        newWidth = newWidth.roundToNearest(1f, .15f)

        // Scale the platform correctly, taking in consideration its rotation and the scaling direction
        val localLeft = if (toLeft) -(newWidth - image.width) else 0f
        val localRight = if (toRight) (newWidth - image.width) else 0f
        updateObjectPolygon(
            image.leftX, image.bottomY,
            image.width, image.height,
            image.img.rotation,
            localLeft, localRight
        )
        selectedMapObject!!.snap.run {
            if (Math.abs(selectedMapObjectPolygon.leftmostX - snapLeft) <= SnapComponent.RESIZE_SNAP_THRESHOLD ||
                Math.abs(selectedMapObjectPolygon.rightmostX - snapRight) <= SnapComponent.RESIZE_SNAP_THRESHOLD ||
                Math.abs(selectedMapObjectPolygon.bottommostY - snapBottom) <= SnapComponent.RESIZE_SNAP_THRESHOLD ||
                Math.abs(selectedMapObjectPolygon.topmostY - snapTop) <= SnapComponent.RESIZE_SNAP_THRESHOLD
            ) {
                return
            }
        }
        val position = selectedMapObjectPolygon.getRectangleCenter()
        image.width = newWidth
        image.setPosition(position.x, position.y)

        updateEditorObject()

        // In case a listener's touchDragged calls this function after this system is done updating, then these functions
        // wouldn't get called, which would result in a slight jitter movement
        repositionButtons()
        updateOverlaySize()
        repositionOverlay()
    }

    private fun updateEditorObject() {
        val polygon = selectedMapObject!!.polygon
        selectedMapObject!!.editorObject.run {
            when {
                !selectedMapObjectPolygon.leftmostX.approximatelyEqualTo(polygon.leftmostX) -> {
                    isResizingLeftwards = true
                }
                !selectedMapObjectPolygon.rightmostX.approximatelyEqualTo(polygon.rightmostX) -> {
                    isResizingRightwards = true
                }
                !selectedMapObjectPolygon.bottommostY.approximatelyEqualTo(polygon.bottommostY) -> {
                    isResizingDownwards = true
                }
                !selectedMapObjectPolygon.topmostY.approximatelyEqualTo(polygon.topmostY) -> {
                    isResizingUpwards = true
                }
            }
        }
    }

    private fun updateObjectPolygon(
        x: Float, y: Float,
        width: Float, height: Float,
        rotationInDegrees: Float = 0f,
        localLeft: Float = 0f, localRight: Float = 0f
    ) {
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

    private fun repositionButtons() {
        val image = selectedMapObject!!.image
        leftArrowButton.setPosition(0f, 0f)
        rightArrowButton.setPosition(
            leftArrowButton.width + buttonsPaddingX + image.width.metersToPixels / gameCamera.zoom + buttonsPaddingX,
            0f
        )
        deleteButton.run {
            setPosition(rightArrowButton.x, deleteButton.height + buttonsPaddingY)
            icon!!.rotation = 360f - image.img.rotation
        }
        rotateButton.setPosition(rightArrowButton.x, rightArrowButton.y + rightArrowButton.height + buttonsPaddingY)
        horizontalPositionButton.run {
            setPosition(
                overlayLevel1.width / 2f - horizontalPositionButton.width / 2f,
                -height / 2f - image.height.metersToPixels / 2f / gameCamera.zoom - buttonsPaddingX
            )
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
            width =
                leftArrowButton.width + buttonsPaddingX + (image.width.metersToPixels / gameCamera.zoom) + buttonsPaddingX + rightArrowButton.width
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
        val objectCoords = worldToOverlayCameraCoordinates(image.centerX, image.centerY)
        overlayLevel2.run {
            x = objectCoords.x - overlayLevel2.width / 2f
            y = objectCoords.y - leftArrowButton.height / 2f
        }
        overlayLevel1.setPosition(overlayLevel2.x, overlayLevel2.y)
    }

    private fun updateButtonsVisibility() {
        selectedMapObject!!.overlay.run {
            verticalPositionButton.isVisible = showMovementButtons
            horizontalPositionButton.isVisible = showMovementButtons
            rotateButton.isVisible = showRotationButton
            rightArrowButton.isVisible = showResizingButtons
            leftArrowButton.isVisible = showResizingButtons
            deleteButton.isVisible = showDeletionButton
        }
    }

    private fun updateOverlayShown() {
        val overlayLevel = (selectedMapObject as Entity).overlay.overlayLevel
        val showLevel2Overlay =
            selectedMapObject!!.overlay.showResizingButtons || selectedMapObject!!.overlay.showDeletionButton
        overlayLevel1.isVisible = if (showLevel2Overlay) (overlayLevel == 1) else true
        overlayLevel2.isVisible = if (showLevel2Overlay) (overlayLevel == 2) else false
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