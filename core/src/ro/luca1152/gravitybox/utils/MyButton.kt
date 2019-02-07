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

package ro.luca1152.gravitybox.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

class MyButton(skin: Skin, styleName: String) : Button(skin, styleName) {
    var icon: Image? = null
    var iconCell: Cell<Image>? = null
    private var downColor = Color.WHITE
    private var upColor = Color.WHITE
    private var clickRunnable: Runnable? = null

    /**
     * Set the colors for when the button is down (clicked) and up.
     * Affects both the button and the icon inside, if any.
     * @param [upColor] The color when the button is up. Default is [Color.WHITE].
     * @param [downColor] The color when the button is down. Default is [Color.WHITE].
     */
    fun setColors(upColor: Color, downColor: Color) {
        // Update the variables
        this.upColor = upColor
        this.downColor = downColor

        // Update the colors of the button and icon
        color = upColor
        icon?.color = upColor

        // Add listener so when the button is clicked, the colors change.
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                color = downColor
                icon?.color = downColor
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                color = upColor
                icon?.color = upColor

                // If there is any click runnable, it should be ran.
                // It is here and not in addClickRunnable() because I can't override a function after an object (the listener) is created.
                if (isOver(this@MyButton, x, y))
                    clickRunnable?.run()
            }
        })
    }

    /**
     * Adds an icon which is centered in the button.
     * @param [drawableName] The name of the icon included in the [skin].
     */
    fun addIcon(drawableName: String) {
        icon = Image(skin, drawableName)
        iconCell = add(icon)
    }

    /**
     * Set what happens after the button is clicked.
     * @param [clickRunnable] Is ran after clicking the button, at touchUp().
     */
    fun addClickRunnable(clickRunnable: Runnable) {
        this.clickRunnable = clickRunnable
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