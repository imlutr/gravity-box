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

class OverlayPositioningSystem(skin: Skin = Injekt.get(),
                               private val gameCamera: GameCamera = Injekt.get(),
                               private val overlayCamera: OverlayCamera = Injekt.get(),
                               private val overlayStage: OverlayStage = Injekt.get()) : EntitySystem() {
    private val overlayGroup = Group()
    private val leftArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-left-arrow-icon")
        iconCell!!.padLeft(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            if (userObject != null) {
                (userObject as Entity).run {
                    image.width += .5f
                    image.x -= .5f
                }
            }
        })
    }
    private val rightArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-right-arrow-icon")
        iconCell!!.padRight(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            if (userObject != null) {
                (userObject as Entity).image.width += .5f
            }
        })
    }
    private val rotateButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-rotate-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            if (userObject != null) {
                (userObject as Entity).image.img.rotateBy(5f)
                overlayGroup.rotateBy(5f)
            }
        })
    }
    private var selectedObject: Entity? = null
    private val paddingX = 20f
    private val paddingY = 50f


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
            updateGroupSize(selectedObject!!.image)
            repositionGroup(selectedObject!!.image)
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

    private fun updateGroupSize(image: ImageComponent) {
        overlayGroup.width = leftArrowButton.width + paddingX + (image.width.metersToPixels / gameCamera.zoom) + paddingX + rightArrowButton.width
        overlayGroup.height = rightArrowButton.height + paddingY + rotateButton.height
        overlayGroup.originX = overlayGroup.width / 2f
        overlayGroup.originY = leftArrowButton.height / 2f
    }

    private val coords = Vector3()
    private fun repositionGroup(objectImage: ImageComponent) {
        // The coordinates of the center of the object
        val coords = worldToOverlayCameraCoordinates(objectImage.x, objectImage.y)
        overlayGroup.run {
            x = coords.x - overlayGroup.width / 2f
            y = coords.y - leftArrowButton.height / 2f
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

    private fun getSelectedObject(): Entity? {
        val entities = engine.getEntitiesFor(Family.all(SelectedObjectComponent::class.java).get())
        return when {
            entities.size() == 0 -> null
            else -> entities.first()
        }
    }

    override fun removedFromEngine(engine: Engine?) {
        overlayGroup.remove()
    }
}