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
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import ktx.actors.plus
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.components.editor.SnapComponent.Companion.DRAG_SNAP_THRESHOLD
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.editor.MovingMockPlatformEntity
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.button.Button
import ro.luca1152.gravitybox.utils.ui.button.Checkbox
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import ro.luca1152.gravitybox.utils.ui.popup.PopUp
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Positions the overlay. */
class OverlayPositioningSystem(
    private val skin: Skin = Injekt.get(),
    private val uiStage: UIStage = Injekt.get(),
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
            private val scene2D
                get() = (selectedMapObject as Entity).scene2D
            var initialImageWidth = 0f
            var initialImageHeight = 0f
            var initialImageX = 0f
            var initialImageY = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                scene2D.run {
                    initialImageWidth = width
                    initialImageHeight = height
                    initialImageX = centerX
                    initialImageY = centerY
                }
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                scaleMapObject(
                    x, this@apply, selectedMapObject!!, toLeft = true,
                    isDestroyablePlatform = selectedMapObject!!.tryGet(DestroyablePlatformComponent) != null
                )
                mapEntity.map.updateRoundedPlatforms = true
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.resetResizingBooleans()
                selectedMapObject!!.snap.resetSnappedSize()
                if (scene2D.width != initialImageWidth || scene2D.height != initialImageHeight)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        ResizeCommand(
                            selectedMapObject!!,
                            scene2D.width - initialImageWidth, scene2D.height - initialImageHeight,
                            scene2D.centerX - initialImageX, scene2D.centerY - initialImageY
                        )
                    )
                selectedMapObject!!.tryGet(MovingObjectComponent)
                    ?.moved(selectedMapObject, selectedMapObject!!.linkedEntity.entity!!)
            }
        })
    }
    private val rightArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-right-arrow-icon")
        iconCell!!.padRight(-4f) // The icon doesn't LOOK centered
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addListener(object : ClickListener() {
            private val scene2D
                get() = (selectedMapObject as Entity).scene2D
            var initialImageWidth = 0f
            var initialImageHeight = 0f
            var initialImageX = 0f
            var initialImageY = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                scene2D.run {
                    initialImageWidth = width
                    initialImageHeight = height
                    initialImageX = centerX
                    initialImageY = centerY
                }
                return true
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                scaleMapObject(
                    x, this@apply, selectedMapObject!!, toRight = true,
                    isDestroyablePlatform = selectedMapObject!!.tryGet(DestroyablePlatformComponent) != null
                )
                mapEntity.map.updateRoundedPlatforms = true
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.resetResizingBooleans()
                selectedMapObject!!.snap.resetSnappedSize()
                if (scene2D.width != initialImageWidth || scene2D.height != initialImageHeight)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        ResizeCommand(
                            selectedMapObject!!,
                            scene2D.width - initialImageWidth, scene2D.height - initialImageHeight,
                            scene2D.centerX - initialImageX, scene2D.centerY - initialImageY
                        )
                    )
                selectedMapObject!!.tryGet(MovingObjectComponent)
                    ?.moved(selectedMapObject, selectedMapObject!!.linkedEntity.entity!!)
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
            private val scene2d
                get() = (selectedMapObject as Entity).scene2D
            var initialImageRotation = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                rotationLabel.isVisible = true
                initialImageRotation = scene2d.rotation
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
                        mouseCoords.y - scene2d.centerY,
                        mouseCoords.x - scene2d.centerX
                    ) * MathUtils.radiansToDegrees
                )

                // The rotate button is not on the same Ox axis as the map object, which in turn affects the rotation
                val deltaAngle = getAngleBetween(this@apply, scene2d)

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

                if (selectedMapObject!!.tryGet(MovingObjectComponent) != null) {
                    selectedMapObject!!.linkedEntity.entity!!.scene2D.rotation = newRotation
                }

                scene2d.rotation = newRotation
                overlayLevel1.rotation = newRotation
                this@apply.icon!!.rotation = 360f - newRotation

                updateRotationLabel()
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.isRotating = false
                rotationLabel.isVisible = false
                if (scene2d.rotation != initialImageRotation)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        RotateCommand(
                            selectedMapObject!!,
                            scene2d.rotation - initialImageRotation
                        )
                    )
                selectedMapObject!!.tryGet(MovingObjectComponent)
                    ?.moved(selectedMapObject, selectedMapObject!!.linkedEntity.entity!!)
            }

            private fun getAngleBetween(rotateButton: Button, objectScene2D: Scene2DComponent): Float {
                val oldRotation = overlayLevel1.rotation
                overlayLevel1.rotation = 0f // Makes calculating the angle easier
                val buttonCoords = rotateButton.localToScreenCoordinates(Vector2(0f, 0f))
                val objectCenterCoords =
                    objectScene2D.group.localToScreenCoordinates(
                        Vector2(
                            objectScene2D.width / 2f,
                            objectScene2D.height / 2f
                        )
                    )
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
            private val scene2D
                get() = (selectedMapObject as Entity).scene2D
            private var initialImageX = 0f
            private var initialMouseXInWorldCoords = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                initialImageX = scene2D.centerX
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
                    scene2D.centerX = newCenterX
                }

                repositionOverlay()
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.isDraggingHorizontally = false
                selectedMapObject!!.snap.resetSnappedX()
                if (scene2D.centerX != initialImageX)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        MoveCommand(
                            selectedMapObject!!,
                            scene2D.centerX - initialImageX,
                            0f
                        )
                    )
                selectedMapObject!!.tryGet(MovingObjectComponent)
                    ?.moved(selectedMapObject, selectedMapObject!!.linkedEntity.entity!!)
                selectedMapObject!!.tryGet(MockMapObjectComponent)?.let {
                    selectedMapObject!!.linkedEntity.entity!!.movingObject.moved(
                        selectedMapObject!!.linkedEntity.entity, selectedMapObject
                    )
                }
            }
        })
    }
    private val verticalPositionButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-vertical-arrow")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addListener(object : DragListener() {
            private val scene2D
                get() = (selectedMapObject as Entity).scene2D
            private var initialImageY = 0f
            private var initialMouseYInWorldCoords = 0f

            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                initialImageY = scene2D.centerY
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
                if ((selectedMapObject as Entity).tryGet(PlatformComponent) != null ||
                    (selectedMapObject as Entity).tryGet(DestroyablePlatformComponent) != null ||
                    (selectedMapObject as Entity).tryGet(MockMapObjectComponent) != null
                ) {
                    newCenterY = newCenterY.roundToNearest(1f, .125f, .5f)
                } else {
                    newCenterY = newCenterY.roundToNearest(.5f, .125f)
                }

                if (Math.abs(selectedMapObject!!.snap.snapCenterY - newCenterY) <= DRAG_SNAP_THRESHOLD) {
                    return
                } else {
                    scene2D.centerY = newCenterY
                }

                repositionOverlay()
                selectedMapObject!!.polygon.update()
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                selectedMapObject!!.editorObject.isDraggingVertically = false
                selectedMapObject!!.snap.resetSnappedY()
                if (scene2D.centerY != initialImageY)
                    undoRedoEntity.undoRedo.addExecutedCommand(
                        MoveCommand(
                            selectedMapObject!!,
                            0f,
                            scene2D.centerY - initialImageY
                        )
                    )
                selectedMapObject!!.tryGet(MovingObjectComponent)
                    ?.moved(selectedMapObject, selectedMapObject!!.linkedEntity.entity!!)
                selectedMapObject!!.tryGet(MockMapObjectComponent)?.let {
                    selectedMapObject!!.linkedEntity.entity!!.movingObject.moved(
                        selectedMapObject!!.linkedEntity.entity, selectedMapObject
                    )
                }
            }
        })
    }
    private val settingsButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-settings-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        addClickRunnable(Runnable {
            uiStage.addActor(createSettingsPopUp())
        })
    }

    private val selectedMapObject: Entity?
        get() = engine.getEntitiesFor(Family.all(EditorObjectComponent::class.java).get()).filterNullableSingleton { it.editorObject.isSelected }
    private val undoRedoEntity: Entity = engine.getEntitiesFor(Family.all(UndoRedoComponent::class.java).get()).first()
    private val selectedMapObjectPolygon = Polygon().apply { vertices = FloatArray(8) }
    private val labels = Group().apply { this + rotationLabel }
    private val overlayLevel1 =
        Group().apply { this + horizontalPositionButton + verticalPositionButton + rotateButton + deleteButton }
    private val overlayLevel2 = Group().apply { this + leftArrowButton + rightArrowButton + settingsButton }
    private val buttonsPaddingX = 20f
    private val buttonsPaddingY = 50f

    override fun addedToEngine(engine: Engine) {
        mapEntity = engine.getSingleton<MapComponent>()
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
        toRight: Boolean = false,
        isDestroyablePlatform: Boolean = false
    ) {
        if (!toLeft && !toRight) error { "No scale direction given." }
        if (toLeft && toRight) error { "Can't scale in two directions." }

        // Translate [xDragged] to game coords system and subtract half of the button's width so deltaX is 0 in the button's center
        var deltaX = (xDragged.pixelsToMeters - (buttonDragged.width / 2f).pixelsToMeters) * gameCamera.zoom
        if (toRight) deltaX = -deltaX

        val scene2D = linkedMapObject.scene2D
        var newWidth = Math.max(.5f, scene2D.width - deltaX)
        newWidth = if (isDestroyablePlatform) {
            newWidth.roundToNearest(
                (16f + 5.33f).pixelsToMeters,
                (16f + 5.33f).pixelsToMeters,
                0f
            )
        } else {
            newWidth.roundToNearest(1f, .15f)
        }

        // Scale the platform correctly, taking in consideration its rotation and the scaling direction
        val localLeft = if (toLeft) -(newWidth - scene2D.width) else 0f
        val localRight = if (toRight) (newWidth - scene2D.width) else 0f
        updateObjectPolygon(
            scene2D.leftX, scene2D.bottomY,
            scene2D.width, scene2D.height,
            scene2D.rotation,
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
        scene2D.width = newWidth
        if (selectedMapObject!!.tryGet(MovingObjectComponent) != null) {
            selectedMapObject!!.linkedEntity.entity!!.scene2D.run {
                group.children.first().width = newWidth
                width = newWidth
                centerX += position.x - scene2D.centerX
                centerY += position.y - scene2D.centerY
            }
        }
        scene2D.run {
            centerX = position.x
            centerY = position.y
            if (isDestroyablePlatform) {
                linkedMapObject.destroyablePlatform.updateScene2D(scene2D)
            }
        }

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
        val scene2D = selectedMapObject!!.scene2D
        leftArrowButton.setPosition(0f, 0f)
        rightArrowButton.setPosition(
            leftArrowButton.width + buttonsPaddingX + scene2D.width.metersToPixels / gameCamera.zoom + buttonsPaddingX,
            0f
        )
        rotateButton.setPosition(rightArrowButton.x, rightArrowButton.y + rightArrowButton.height + buttonsPaddingY)
        settingsButton.setPosition(rotateButton.x, rotateButton.y)
        horizontalPositionButton.run {
            setPosition(
                overlayLevel1.width / 2f - horizontalPositionButton.width / 2f,
                -height / 2f - scene2D.height.metersToPixels / 2f / gameCamera.zoom - buttonsPaddingX
            )
            icon!!.rotation = 360f - scene2D.rotation
        }
        verticalPositionButton.run {
            setPosition(rightArrowButton.x, rightArrowButton.y)
            icon!!.rotation = 360f - scene2D.rotation
        }
        deleteButton.run {
            setPosition(leftArrowButton.x, verticalPositionButton.y)
            icon!!.rotation = 360f - scene2D.rotation
        }
    }

    private fun updateOverlaySize() {
        val scene2D = selectedMapObject!!.scene2D
        overlayLevel2.run {
            width =
                leftArrowButton.width + buttonsPaddingX + (scene2D.width.metersToPixels / gameCamera.zoom) + buttonsPaddingX + rightArrowButton.width
            height = rightArrowButton.height + buttonsPaddingY + rotateButton.height
            setOrigin(width / 2f, leftArrowButton.height / 2f)
            rotation = scene2D.rotation
        }
        overlayLevel1.run {
            setSize(overlayLevel2.width, overlayLevel2.height)
            setOrigin(overlayLevel2.originX, overlayLevel2.originY)
            rotation = overlayLevel2.rotation
        }
    }

    private fun repositionOverlay() {
        val scene2D = selectedMapObject!!.scene2D
        val objectCoords = worldToOverlayCameraCoordinates(scene2D.centerX, scene2D.centerY)
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
            selectedMapObject!!.overlay.showResizingButtons || selectedMapObject!!.overlay.showSettingsButton
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

    private fun createSettingsPopUp() = PopUp(500f, 310f, skin).apply {
        widget.run {
            add(createDestroyableCheckbox()).padBottom(20f).expandX().left().row()
            add(createMovingCheckbox()).padBottom(20f).expandX().left().row()
            add(createRotatingCheckbox()).expandX().left().row()
        }
    }

    private fun createDestroyableCheckbox() = Table(skin).apply {
        val checkbox = Checkbox(skin).apply {
            isTicked = selectedMapObject!!.tryGet(DestroyablePlatformComponent) != null
            tickRunnable = Runnable {
                selectedMapObject!!.run {
                    removeComponent<PlatformComponent>()
                    destroyablePlatform()
                    destroyablePlatform.updateScene2D(selectedMapObject!!.scene2D)
                }
            }
            untickRunnable = Runnable {
                selectedMapObject!!.run {
                    removeComponent<DestroyablePlatformComponent>()
                    platform()
                }
                mapEntity.map.updateRoundedPlatforms = true
            }
        }
        val label = DistanceFieldLabel("Destroyable", skin, "bold", 65f, Colors.gameColor)
        add(checkbox).padRight(20f)
        add(label)
    }

    private fun createMovingCheckbox() = Table(skin).apply {
        val checkbox = Checkbox(skin).apply {
            isTicked = selectedMapObject!!.tryGet(MovingObjectComponent) != null
            tickRunnable = Runnable {
                selectedMapObject!!.run {
                    movingObject(scene2D.centerX + 1f, scene2D.centerY + 1f)
                    val mockPlatform = MovingMockPlatformEntity.createEntity(
                        selectedMapObject!!,
                        movingObject.endPoint.x, movingObject.endPoint.y,
                        scene2D.width, scene2D.height
                    )
                    mockPlatform.linkedEntity(selectedMapObject!!)
                    linkedEntity(mockPlatform)
                }
            }
            untickRunnable = Runnable {
                selectedMapObject!!.run {
                    removeComponent<MovingObjectComponent>()
                    engine.removeEntity(linkedEntity.entity)
                    removeComponent<LinkedEntityComponent>()
                }
            }
        }
        val label = DistanceFieldLabel("Moving", skin, "bold", 65f, Colors.gameColor)
        add(checkbox).padRight(20f)
        add(label)
    }

    private fun createRotatingCheckbox() = Table(skin).apply {
        val checkbox = Checkbox(skin)
        val label = DistanceFieldLabel("Rotating", skin, "bold", 65f, Colors.gameColor)
        add(checkbox).padRight(20f)
        add(label)
    }
}