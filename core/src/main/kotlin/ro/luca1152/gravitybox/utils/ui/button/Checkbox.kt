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

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ro.luca1152.gravitybox.utils.ui.Colors

/** A checkbox capable of being ticked and unticked. */
class Checkbox(skin: Skin) : ClickButton(skin, "checkbox") {
    /** What happens when the checkbox is ticked. */
    val tickRunnable: Runnable? = null

    /** What happens when the checkbox is unticked. */
    val untickRunnable: Runnable? = null

    /** True if the [Checkbox] is ticked. */
    var isTicked = false
        set(value) {
            field = value
            icon!!.isVisible = value
        }

    init {
        addIcon("checkbox-x-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        icon!!.isVisible = false
        addClickRunnable(Runnable {
            isTicked = !isTicked
            if (isTicked) {
                tickRunnable?.run()
                icon!!.isVisible = true
            } else {
                untickRunnable?.run()
                icon!!.isVisible = false
            }
        })
    }
}