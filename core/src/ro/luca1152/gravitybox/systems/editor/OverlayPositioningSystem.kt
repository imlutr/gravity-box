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
    }
    private val rightArrowButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-right-arrow-icon")
        iconCell!!.padRight(-4f) // The icon doesn't LOOK centered
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
    }
    private val rotateButton: ClickButton = ClickButton(skin, "small-round-button").apply {
        addIcon("small-rotate-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
    }
    private var selectedObject: Entity? = null

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
        } else {
            overlayGroup.isVisible = true
            repositionButtons(selectedObject!!.image)
        }
    }

    private val coords = Vector3()

    private fun repositionButtons(image: ImageComponent) {
        // The coordinates of the bottom left corner of the image
        val coords = worldToOverlayCameraCoordinates(image.img.x, image.img.y)
        val zoomedWidth = image.width.metersToPixels / gameCamera.zoom
        val zoomedHeight = image.height.metersToPixels / gameCamera.zoom

        leftArrowButton.setPosition(coords.x - leftArrowButton.width - 20f, coords.y + zoomedHeight / 2f - leftArrowButton.height / 2f)
        rightArrowButton.setPosition(coords.x + zoomedWidth + 20f, coords.y + zoomedHeight / 2f - rightArrowButton.height / 2f)
        rotateButton.setPosition(rightArrowButton.x, rightArrowButton.y + rightArrowButton.height + 50f)
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