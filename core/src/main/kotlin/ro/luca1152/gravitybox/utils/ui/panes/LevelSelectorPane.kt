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

package ro.luca1152.gravitybox.utils.ui.panes

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class LevelSelectorPane(context: Context) : Pane(context, 600f, 736f, context.inject()) {
    // Injected objects
    private val skin: Skin = context.inject()

    // Buttons
    private val closeButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(context, "Close", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@LevelSelectorPane.hide()
            }
        })
    }

    init {
        updateWidget()
    }

    private fun updateWidget() {
        widget.run {
            add(closeButton).width(492f).expand().bottom().row()
        }
    }
}