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
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import ktx.actors.plus
import ktx.math.minus
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.entities.EntityFactory
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
                               private val overlayStage: OverlayStage = Injekt.get()) : EntitySystem() {
    private val leftArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-left-arrow-icon")
        iconCell!!.padLeft(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : ClickListener() {
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                scaleMapObject(x, y, this@apply, userObject as Entity, toLeft = true)
            }
        })
    }
    private val rightArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-right-arrow-icon")
        iconCell!!.padRight(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : ClickListener() {
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                scaleMapObject(x, y, this@apply, userObject as Entity, toRight = true)
            }
        })
    }
    private val rotateButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-rotate-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : DragListener() {
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
                val image = (userObject as Entity).image

                val mouseCoords = screenToWorldCoordinates(Gdx.input.x, Gdx.input.y)
                var newRotation = toPositiveAngle(MathUtils.atan2(mouseCoords.y - image.y, mouseCoords.x - image.x) * MathUtils.radiansToDegrees)

                // The rotate button is not on the same Ox axis as the map object, which in turn affects the rotation
                val deltaAngle = getAngleBetween(this@apply, image)

                newRotation -= deltaAngle
                newRotation = MathUtils.round(newRotation).toFloat()
                newRotation = newRotation.roundToNearest(45f, 5f)
                newRotation = toPositiveAngle(newRotation)

                image.img.rotation = newRotation
                overlayGroupLv2.rotation = newRotation
                this@apply.icon!!.rotation = 360f - newRotation

                updateLabels()
                rotationLabel.isVisible = true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                rotationLabel.isVisible = false
            }

            private fun getAngleBetween(rotateButton: Button, objectImage: ImageComponent): Float {
                val oldRotation = overlayGroupLv2.rotation
                overlayGroupLv2.rotation = 0f // Makes calculating the angle easier
                val buttonCoords = rotateButton.localToScreenCoordinates(Vector2(0f, 0f))
                val objectCenterCoords = objectImage.img.localToScreenCoordinates(Vector2(objectImage.width / 2f, objectImage.height / 2f))
                overlayGroupLv2.rotation = oldRotation
                return Math.abs(MathUtils.atan2(buttonCoords.y - objectCenterCoords.y, buttonCoords.x - objectCenterCoords.x) * MathUtils.radiansToDegrees)
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
                get() = (selectedObject as Entity).image
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
                image.x = image.x.roundToNearest(.5f, .1f)

                repositionOverlay()
                updateLabels()
            }
        })
    }
    private val verticalPositionButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-vertical-arrow")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addListener(object : DragListener() {
            private val image
                get() = (selectedObject as Entity).image
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
                image.y = image.y.roundToNearest(1f, .2f, .5f)

                repositionOverlay()
                updateLabels()
            }
        })
    }

    /** The [Entity] that contains the selected map object. */
    private var selectedObject: Entity? = null
    /** A polygon that has the same size and position as the [selectedObject]. */
    private val selectedObjectPolygon = Polygon().apply { vertices = FloatArray(8) }

    private val labelsGroup = Group().apply { this + rotationLabel }

    /** The first level of the overlay (shown when a map object is touched once). */
    private val overlayGroupLv1 = Group().apply { this + horizontalPositionButton + verticalPositionButton }

    /** The second level of the overlay (shown when a map object is touched twice). */
    private val overlayGroupLv2 = Group().apply { this + leftArrowButton + rightArrowButton + rotateButton }
    /** The padding of the overlay buttons on the X axis. */
    private val paddingX = 20f
    /** The padding of the overlay buttons on the Y axis. */
    private val paddingY = 50f

    override fun addedToEngine(engine: Engine?) {
        overlayStage.run {
            addActor(labelsGroup)
            addActor(overlayGroupLv1)
            addActor(overlayGroupLv2)
        }
    }

    override fun update(deltaTime: Float) {
        selectedObject = getSelectedObject()
        if (selectedObject == null) {
            setButtonsUserObject(null)
            overlayGroupLv1.isVisible = false
            overlayGroupLv2.isVisible = false
            labelsGroup.isVisible = false
        } else {
            setButtonsUserObject(selectedObject)
            updateOverlaySize()
            updateButtonsPosition()
            repositionOverlay()
            updateLabels()
            updateOverlayBasedOnLevel()
            labelsGroup.isVisible = true
        }
    }

    override fun removedFromEngine(engine: Engine?) {
        labelsGroup.remove()
        overlayGroupLv1.remove()
        overlayGroupLv2.remove()
    }

    val debugEntity = EntityFactory.createDebugEntity()
    val debugEntity1 = EntityFactory.createDebugEntity()

    private fun getDistanceBetween(actorA: Actor, actorB: Actor, targetStage: Stage,
                                   localCoordsA: Vector2 = Vector2(0f, 0f), localCoordsB: Vector2 = Vector2(0f, 0f),
                                   uiStage: UIStage = Injekt.get()): Vector2 {
        val actorAStageCoords = actorA.localToStageCoordinates(Vector2(localCoordsA))
        actorAStageCoords.scl(1 / 64f)
        debugEntity.debug.set(actorAStageCoords)

        val actorBStageCoords = actorB.localToStageCoordinates(Vector2(localCoordsB))
        debugEntity1.debug.set(actorBStageCoords)

        var actorAScreenCoords = localCoordsA
        actorAScreenCoords = actorA.localToScreenCoordinates(actorAScreenCoords)

        var actorBScreenCoords = localCoordsB
        actorBScreenCoords = actorB.localToScreenCoordinates(actorBScreenCoords)

        // The coordinates are now relative
        actorAScreenCoords -= actorBScreenCoords

        // The coordinates are now in targetStage coordinates
        val ppm = uiStage.viewport.worldWidth / targetStage.viewport.worldWidth
        actorAScreenCoords.scl(1f / ppm * (targetStage.camera as OrthographicCamera).zoom)

        return actorAScreenCoords
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
        val position = getRectangleCenter(selectedObjectPolygon)
        image.width = newWidth
        image.setPosition(position.x, position.y)

        // In case a listener's touchDragged calls this function after this system is done updating, then these functions
        // wouldn't get called, which would result in a slight jitter movement
        updateButtonsPosition()
        updateOverlaySize()
        repositionOverlay()
    }

    /**
     * ([x],[y]) the bottom left corner coordinates;
     * [rotation] in degrees;
     * [width], [height] the dimensions of the object before any transformations were made to it;
     * [localLeft], [localRight] the local x of the left and right side of the transformed (e.g. resized) object
     */
    private fun updateObjectPolygon(x: Float, y: Float, width: Float, height: Float, rotation: Float = 0f, localLeft: Float = 0f, localRight: Float = 0f) {
        selectedObjectPolygon.run {
            setRotation(0f)
            vertices.run {
                set(0, localLeft); set(1, 0f) // bottom left corner
                set(2, width + localRight); set(3, 0f) // bottom right corner
                set(4, width + localRight); set(5, height) // top right corner
                set(6, localLeft); set(7, height) // top left corner
            }
            setPosition(x, y)
            setOrigin(width / 2f, height / 2f)
            setRotation(rotation)
        }
    }

    private val tmpVec2 = Vector2()
    /** Only works for a [Polygon] with 4 vertices. */
    private fun getRectangleCenter(rectangle: Polygon): Vector2 {
        rectangle.transformedVertices.run {
            tmpVec2.set((get(0) + get(4)) / 2f, (get(1) + get(5)) / 2f)
        }
        return tmpVec2
    }

    private fun getSelectedObject(): Entity? {
        val entities = engine.getEntitiesFor(Family.all(SelectedObjectComponent::class.java).get())
        return when {
            entities.size() == 0 -> null
            else -> entities.first()
        }
    }

    private fun setButtonsUserObject(obj: Any?) {
        leftArrowButton.userObject = obj
        rightArrowButton.userObject = obj
        rotateButton.userObject = obj
    }

    private fun updateButtonsPosition() {
        val image = selectedObject!!.image
        leftArrowButton.run {
            x = 0f
            y = 0f
        }
        rightArrowButton.run {
            x = leftArrowButton.width + paddingX + image.width.metersToPixels / gameCamera.zoom + paddingX
            y = 0f
        }
        rotateButton.run {
            x = rightArrowButton.x
            y = rightArrowButton.y + rightArrowButton.height + paddingY
        }
        horizontalPositionButton.run {
            x = overlayGroupLv1.width / 2f - horizontalPositionButton.width / 2f
            y = -height
            icon!!.rotation = 360f - image.img.rotation
        }
        verticalPositionButton.run {
            x = rightArrowButton.x
            y = rightArrowButton.y
            icon!!.rotation = 360f - image.img.rotation
        }
    }

    private fun updateOverlaySize() {
        val image = selectedObject!!.image
        overlayGroupLv2.run {
            width = leftArrowButton.width + paddingX + (image.width.metersToPixels / gameCamera.zoom) + paddingX + rightArrowButton.width
            height = rightArrowButton.height + paddingY + rotateButton.height
            setOrigin(width / 2f, leftArrowButton.height / 2f)
            rotation = image.img.rotation
        }
        overlayGroupLv1.run {
            setSize(overlayGroupLv2.width, overlayGroupLv2.height)
            setOrigin(overlayGroupLv2.originX, overlayGroupLv2.originY)
            rotation = overlayGroupLv2.rotation
        }
    }

    private val coords = Vector3()
    private fun repositionOverlay() {
        val image = selectedObject!!.image
        val objectCoords = worldToOverlayCameraCoordinates(image.x, image.y)
        overlayGroupLv2.run {
            x = objectCoords.x - overlayGroupLv2.width / 2f
            y = objectCoords.y - leftArrowButton.height / 2f
        }
        overlayGroupLv1.setPosition(overlayGroupLv2.x, overlayGroupLv2.y)
    }

    private fun updateLabels() {
        rotationLabel.run {
            setText("${overlayGroupLv2.rotation.toInt()}°")
            val pos = rotateButton.localToStageCoordinates(Vector2(rotateButton.width / 2f, rotateButton.height / 2f))
            x = pos.x - prefWidth / 2f
            y = pos.y + rotateButton.height / 2f + paddingY / 2f - height / 2f
        }
    }

    private fun updateOverlayBasedOnLevel() {
        val level = (selectedObject as Entity).selectedObject.level
        overlayGroupLv1.isVisible = (level == 1)
        overlayGroupLv2.isVisible = (level == 2)
    }

    private fun worldToOverlayCameraCoordinates(x: Float, y: Float): Vector3 {
        coords.run {
            this.x = x
            this.y = y
        }

        // [coords] are now in screen coordinates
        gameCamera.project(coords)

        // When you unproject coordinates, the (0;0) is in the top left corner, when usually
        // it is in the bottom left corner, so this must be corrected for the function to work
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