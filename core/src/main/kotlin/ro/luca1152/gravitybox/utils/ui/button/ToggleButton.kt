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

/** A [Button] which toggles when clicked on. */
class ToggleButton(skin: Skin, styleName: String) : Button(skin, styleName) {
    private var toggleRunnable: Runnable? = null

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
        this.upColor = upColor
        this.downColor = downColor
        color = upColor
        icon?.color = upColor
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                // Return true so touchUp can be called
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (!isToggled) {
                    toggleButtonOn()
                    if (isOver(this@ToggleButton, x, y))
                        toggleRunnable?.run()
                }
            }

            private fun toggleButtonOn() {
                toggledButton.get()?.isToggled = false
                isToggled = true
                color = downColor
                icon?.color = downColor
            }
        })
    }
}