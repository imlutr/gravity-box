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
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ro.luca1152.gravitybox.utils.kotlin.Reference
import ro.luca1152.gravitybox.utils.kotlin.setWithoutAlpha
import ro.luca1152.gravitybox.utils.ui.Colors

/** My own extension of the [Button] class. */
abstract class Button(
    skin: Skin,
    var styleName: String
) : Button(skin, styleName) {
    var icon: Image? = null
    var iconCell: Cell<Image>? = null
    var opaqueImage: Image? = null
    var toggledButton = Reference<ToggleButton>()
    var toggleOffButtons = false
    var type = ButtonType.DEFAULT_BUTTON_TYPE
    var upColor: Color = Color.WHITE
    var downColor: Color = Color.WHITE
    var syncColorsWithColorScheme = true

    init {
        super.setOrigin(width / 2f, height / 2f)
    }

    abstract fun setColors(upColor: Color, downColor: Color)

    override fun act(delta: Float) {
        super.act(delta)
        if (syncColorsWithColorScheme)
            syncColors()
    }

    private fun syncColors() {
        // TODO: use downColor and upColor instead of referencing colors directly
        val isToggled = if (userObject == null) false else userObject as Boolean
        when (styleName) {
            "color-round-button" -> {
                when (isPressed || isToggled) {
                    true -> color.setWithoutAlpha(Colors.uiDownColor)
                    false -> color.setWithoutAlpha(Colors.gameColor)
                }
                icon?.color?.setWithoutAlpha(Colors.bgColor)
            }
            "empty-round-button" -> {
                color.set(Colors.uiWhite)
                icon?.color?.set(Colors.uiWhite)
            }
            "white-full-round-button" -> {
                color.set(Colors.uiWhite)
                icon?.color?.set(Colors.uiGray)
            }
            "gray-full-round-button" -> {
                color.set(Colors.uiGray)
                icon?.color?.set(Colors.uiWhite)
            }
            else -> when (isPressed || isToggled) {
                true -> {
                    color.setWithoutAlpha(Colors.uiDownColor)
                    icon?.color?.setWithoutAlpha(Colors.uiDownColor)
                    opaqueImage?.color?.setWithoutAlpha(Colors.bgColor)
                }
                false -> {
                    color.setWithoutAlpha(Colors.gameColor)
                    icon?.color?.setWithoutAlpha(Colors.gameColor)
                    opaqueImage?.color?.setWithoutAlpha(Colors.bgColor)
                }
            }
        }
    }

    /**
     * Adds an icon which is centered in the button.
     * @param [drawableName] The name of the icon included in the [skin].
     */
    fun addIcon(drawableName: String) {
        icon = Image(skin, drawableName)
        icon!!.setOrigin(icon!!.width / 2f, icon!!.height / 2f)
        iconCell = add(icon)
    }

    /**
     * Sets whether it should toggle of every other ToggleButton after you click on it.
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
    fun setToggledButtonReference(toggledButton: Reference<ToggleButton>) {
        this.toggledButton = toggledButton
    }

    /**
     * Set whether the inside of the button should be opaque or transparent.
     * @param [opaque] Is false by default.
     */
    fun setOpaque(opaque: Boolean) {
        if (opaque) {
            check(skin.getDrawable("$styleName-inside") != null)
            { "Can't make the button opaque: no inside texture was given for the button style used." }

            opaqueImage = Image(skin.getDrawable("$styleName-inside")).apply {
                setPosition(
                    this@Button.width / 2f - width / 2f,
                    this@Button.width / 2f - height / 2f
                )
                color = Colors.bgColor
            }
            addActor(opaqueImage)

            // Make the icon, if there's any, visible
            icon?.toFront()
        } else {
            opaqueImage?.remove()
        }
    }
}

enum class ButtonType {
    DEFAULT_BUTTON_TYPE,
    MOVE_TOOL_BUTTON,
    PLACE_TOOL_BUTTON
}