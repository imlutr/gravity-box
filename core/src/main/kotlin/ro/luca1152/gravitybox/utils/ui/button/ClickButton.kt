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

package ro.luca1152.gravitybox.utils.ui.button

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

open class ClickButton(skin: Skin, styleName: String) : Button(skin, styleName) {
    private var clickRunnable: Runnable? = null
    private var clickListener = object : ClickListener() {
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
            if (isOver(this@ClickButton, x, y)) {
                // Toggle off every other button if the click button was set to do so
                if (toggleOffButtons) {
                    toggledButton.get()?.isToggled = false
                    stage.root.findActor<PaneButton>("PaneButton")?.clickedOutsidePane()
                }
                clickRunnable?.run()
            }
        }
    }

    init {
        super.addListener(clickListener)
    }

    /**
     * Set what happens after the button is clicked.
     * @param [clickRunnable] Is ran after clicking the button, at touchUp().
     */
    fun addClickRunnable(clickRunnable: Runnable) {
        this.clickRunnable = clickRunnable
    }

    override fun setColors(upColor: Color, downColor: Color) {
        // Update the variables
        this.upColor = upColor
        this.downColor = downColor

        // Initialize the colors, if it's the case
        if (color == Color.WHITE) {
            color = upColor
            icon?.color = upColor
        }
    }
}