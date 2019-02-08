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

package ro.luca1152.gravitybox.utils.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ro.luca1152.gravitybox.utils.kotlin.Reference

abstract class MyButton(skin: Skin, styleName: String) : Button(skin, styleName) {
    var icon: Image? = null
    var iconCell: Cell<Image>? = null
    var toggledButton = Reference<MyToggleButton>()
    var toggleOffButtons = false
    var upColor: Color = Color.WHITE
    var downColor: Color = Color.WHITE

    /**
     * Set the colors for when the button is down (clicked) and up.
     * Affects both the button and the icon inside, if any.
     * @param [upColor] The color when the button is up. Default is [Color.WHITE].
     * @param [downColor] The color when the button is down. Default is [Color.WHITE].
     */
    abstract fun setColors(upColor: Color, downColor: Color)

    /**
     * Adds an icon which is centered in the button.
     * @param [drawableName] The name of the icon included in the [skin].
     */
    fun addIcon(drawableName: String) {
        icon = Image(skin, drawableName)
        iconCell = add(icon)
    }

    /**
     * Sets whether it should toggle of every other MyToggleButton after you click on it.
     * @param [toggleOff] Default is false.
     */
    fun setToggleOffEveryOtherButton(toggleOff: Boolean) {
        this.toggleOffButtons = toggleOff
    }

    /**
     * Keep track of which button is currently toggled on (selected) to determine whether
     * the button should be toggled off.
     * @param [toggledButton] Its reference can be null.
     */
    fun setToggledButtonReference(toggledButton: Reference<MyToggleButton>) {
        this.toggledButton = toggledButton
    }

    /**
     * Set whether the inside of the button should be opaque or transparent.
     * @param [opaque] Is false by default.
     */
    fun setOpaque(opaque: Boolean) {
        if (opaque) {
            // Add the inside texture of the button and set its color to the background color
            addActor(Image(skin.getDrawable("small-button-inside")).apply {
                setPosition(this@MyButton.width / 2f - width / 2f, this@MyButton.width / 2f - height / 2f)
                color = ColorScheme.currentLightColor
            })

            // Make the icon, if there's any, visible
            icon?.toFront()
        }
    }
}