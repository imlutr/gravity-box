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
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.actors.plus
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.OverlayCamera
import ro.luca1152.gravitybox.utils.kotlin.OverlayStage
import ro.luca1152.gravitybox.utils.kotlin.Reference
import ro.luca1152.gravitybox.utils.ui.ClickButton
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class OverlayRenderingSystem(private val focusedObject: Reference<Image>,
                             private val skin: Skin = Injekt.get(),
                             private val gameCamera: GameCamera = Injekt.get(),
                             private val overlayCamera: OverlayCamera = Injekt.get(),
                             private val overlayStage: OverlayStage = Injekt.get()) : EntitySystem() {
    private lateinit var leftArrowButton: ClickButton
    private lateinit var rightArrowButton: ClickButton
    private lateinit var rotateButton: ClickButton

    override fun addedToEngine(engine: Engine?) {
        leftArrowButton = ClickButton(skin, "small-round-button").apply {
            addIcon("small-left-arrow-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }
        rightArrowButton = ClickButton(skin, "small-round-button").apply {
            addIcon("small-right-arrow-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }
        rotateButton = ClickButton(skin, "small-round-button").apply {
            addIcon("small-rotate-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }
        overlayStage + leftArrowButton + rightArrowButton + rotateButton
    }

    override fun update(deltaTime: Float) {
        syncOverlayCamera(with = gameCamera)
        repositionButtons()
        overlayStage.act(deltaTime)
        overlayStage.draw()
    }

    private fun syncOverlayCamera(with: Camera) {
        overlayCamera.position.set(with.position)
    }

    private fun repositionButtons() {
        if (focusedObject.get() == null) {
            leftArrowButton.isVisible = false
            rightArrowButton.isVisible = false
            rotateButton.isVisible = false
        } else {
            leftArrowButton.isVisible = true
            rightArrowButton.isVisible = true
            rotateButton.isVisible = true
        }
    }
}