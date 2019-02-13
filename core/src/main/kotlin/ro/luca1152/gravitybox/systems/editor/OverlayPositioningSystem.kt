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
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ro.luca1152.gravitybox.components.ImageComponent
import ro.luca1152.gravitybox.components.SelectedObjectComponent
import ro.luca1152.gravitybox.components.image
import ro.luca1152.gravitybox.metersToPixels
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.OverlayCamera
import ro.luca1152.gravitybox.utils.kotlin.OverlayStage
import ro.luca1152.gravitybox.utils.ui.ClickButton
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Positions the overlay over the selected platform. */
class OverlayPositioningSystem(skin: Skin = Injekt.get(),
                               private val gameCamera: GameCamera = Injekt.get(),
                               private val overlayCamera: OverlayCamera = Injekt.get(),
                               private val overlayStage: OverlayStage = Injekt.get()) : EntitySystem() {
    private val leftArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-left-arrow-icon")
        iconCell!!.padLeft(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            if (userObject != null) {
                (userObject as Entity).run {
                    updateObjectPolygon(image.img.x, image.img.y, image.width, image.height, image.img.rotation, -.5f, 0f)
                    val position = getRectangleCenter(selectedObjectPolygon)
                    image.run {
                        width += .5f
                        setPosition(position.x, position.y)
                    }
                }
            }
        })
    }
    private val rightArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-right-arrow-icon")
        iconCell!!.padRight(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            if (userObject != null)
                (userObject as Entity).run {
                    updateObjectPolygon(image.img.x, image.img.y, image.width, image.height, image.img.rotation, 0f, .5f)
                    val position = getRectangleCenter(selectedObjectPolygon)
                    image.run {
                        width += .5f
                        setPosition(position.x, position.y)
                    }
                }
        })
    }
    private val rotateButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-rotate-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            if (userObject != null) {
                (userObject as Entity).run {
                    image.img.rotateBy(10f)
                }
                overlayGroup.rotateBy(10f)
            }
        })
    }
    private val overlayGroup = Group()
    private var selectedObject: Entity? = null
    private val paddingX = 20f // The padding of the overlay buttons on the X axis
    private val paddingY = 50f // The padding of the overlay buttons on the Y axis
    private val selectedObjectPolygon = Polygon().apply { vertices = FloatArray(8) } // A polygon which uses the size and position of the selected object

    init {
        overlayGroup.run {
            addActor(leftArrowButton)
            addActor(rightArrowButton)
            addActor(rotateButton)
        }
    }

    override fun addedToEngine(engine: Engine?) {
        overlayStage.addActor(overlayGroup)
    }

    override fun update(deltaTime: Float) {
        selectedObject = getSelectedObject()
        if (selectedObject == null) {
            overlayGroup.isVisible = false
            setUserObjectForButtons(null)
        } else {
            overlayGroup.isVisible = true
            setUserObjectForButtons(selectedObject)
            updateButtonsPositionInGroup(selectedObject!!.image)
            updateOverlaySize(selectedObject!!.image)
            repositionOverlay(selectedObject!!.image)
        }
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

    private fun setUserObjectForButtons(obj: Any?) {
        leftArrowButton.userObject = obj
        rightArrowButton.userObject = obj
        rotateButton.userObject = obj
    }

    private fun updateButtonsPositionInGroup(objectImage: ImageComponent) {
        leftArrowButton.run {
            x = 0f
            y = 0f
        }
        rightArrowButton.run {
            x = leftArrowButton.width + paddingX + objectImage.width.metersToPixels / gameCamera.zoom + paddingX
            y = 0f
        }
        rotateButton.run {
            x = rightArrowButton.x
            y = rightArrowButton.y + rightArrowButton.height + paddingY
        }
    }

    private fun updateOverlaySize(image: ImageComponent) {
        overlayGroup.run {
            width = leftArrowButton.width + paddingX + (image.width.metersToPixels / gameCamera.zoom) + paddingX + rightArrowButton.width
            height = rightArrowButton.height + paddingY + rotateButton.height
            originX = overlayGroup.width / 2f
            originY = leftArrowButton.height / 2f
        }
    }

    private val coords = Vector3()
    private fun repositionOverlay(objectImage: ImageComponent) {
        val objectCoords = worldToOverlayCameraCoordinates(objectImage.x, objectImage.y)
        overlayGroup.run {
            x = objectCoords.x - overlayGroup.width / 2f
            y = objectCoords.y - leftArrowButton.height / 2f
        }
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

    override fun removedFromEngine(engine: Engine?) {
        overlayGroup.remove()
    }
}