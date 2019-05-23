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
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel

class ClickTextButton(
    context: Context,
    buttonStyleName: String,
    skin: Skin,
    text: String,
    labelStyleName: String,
    textSize: Float = 32f,
    private val usesGameColor: Boolean = true
) : Button(skin, buttonStyleName) {
    val label = DistanceFieldLabel(context, text, skin, labelStyleName, fontSize = textSize).apply {
        this@ClickTextButton.add(this)
    }
    private val clickListener = object : ClickListener() {
        override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
            super.touchDown(event, x, y, pointer, button)
            label.color = downColor
            return true
        }

        override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
            super.touchUp(event, x, y, pointer, button)
            label.color = upColor
        }

        override fun clicked(event: InputEvent?, x: Float, y: Float) {
            super.clicked(event, x, y)
            clickRunnable?.run()
        }
    }
    var clickRunnable: Runnable? = null
    var upColor = Color.WHITE!!
        set(value) {
            field = value
            if (!clickListener.isPressed) {
                label.color = upColor
            }
        }
    var downColor = Color.WHITE!!
        set(value) {
            field = value
            if (clickListener.isPressed) {
                label.color = downColor
            }
        }

    override fun act(delta: Float) {
        super.act(delta)
        if (usesGameColor) {
            if (isPressed) {
                label.color = Colors.uiDownColor
            } else {
                label.color = Colors.gameColor
            }
        } else {
            if (isPressed) {
                label.color = Colors.uiDownColor
            } else {
                label.color = Colors.bgColor
            }
        }
    }

    init {
        addListener(clickListener)
    }
}