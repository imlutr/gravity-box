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

package ro.luca1152.gravitybox.utils.ui.popup

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel

class TextPopUp(
    context: Context,
    width: Float, height: Float,
    text: CharSequence,
    skin: Skin, fontName: String,
    textSize: Float = 32f,
    textColor: Color = Color.WHITE
) : PopUp(context, width, height, skin) {
    private val textLabel = DistanceFieldLabel(context, text, skin, fontName, textSize, textColor).apply {
        setWrap(true)
        setAlignment(Align.center, Align.center)
    }

    init {
        widget.add(textLabel).prefWidth(width - 50f).expand().center()
    }

    override fun act(delta: Float) {
        super.act(delta)
        textLabel.color = Colors.gameColor
    }
}