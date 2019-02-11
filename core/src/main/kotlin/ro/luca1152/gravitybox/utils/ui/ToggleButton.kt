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
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

/**
 * A button which instead of automatically toggling of after you click on it,
 * it toggles off only after you click on it again or select another button.
 */
class ToggleButton(skin: Skin, styleName: String) : Button(skin, styleName) {
    private var toggleRunnable: Runnable? = null

    /** If true, then the button's colors becomes [downColor]. */
    var isToggled = false
        set(value) {
            field = value
            when (value) {
                true -> {
                    color = downColor
                    icon?.color = downColor
                    toggledButton.set(this)
                }
                false -> {
                    color = upColor
                    icon?.color = upColor
                    toggledButton.set(null)
                }
            }
        }

    init {
        setToggleOffEveryOtherButton(true)
    }

    override fun setColors(upColor: Color, downColor: Color) {
        // Update the variables
        this.upColor = upColor
        this.downColor = downColor

        // Update the colors of the button and icon
        color = upColor
        icon?.color = upColor

        // Add listener so when the button is clicked, the colors change.
        addListener(object : ClickListener() {
            override fun touchDown(
                event: InputEvent?,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                // Do nothing, because colors change only at touchUp
                return true
            }

            override fun touchUp(
                event: InputEvent?,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ) {
                when (isToggled) {
                    // If the button is toggled on, then do nothing
                    true -> {
                    }

                    // The button wasn't toggled off, so it should now be toggled on
                    false -> {
                        toggledButton.get()?.isToggled = false

                        isToggled = true
                        color = downColor
                        icon?.color = downColor
                    }
                }

                // If the button was toggled ON, then the toggleRunnable should be ran
                if (isToggled && isOver(this@ToggleButton, x, y))
                    toggleRunnable?.run()
            }
        })
    }

    /**
     * Set what happens after the button is toggled ON.
     * @param [toggleRunnable] Is ran after toggling on the button, at touchUp().
     */
    fun addToggleRunnable(toggleRunnable: Runnable) {
        this.toggleRunnable = toggleRunnable
    }
}