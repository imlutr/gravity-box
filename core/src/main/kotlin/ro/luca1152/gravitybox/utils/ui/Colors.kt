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

/**
 * Contains every color used in the game. The color variable names follow the structure
 * [word]-[V], where the V value is the value of the color's HSV (hue, saturation, value) representation.
 *
 * The higher the V is, the lighter the color is.
 */
object Color {
    /**
     * The hue of the game's color scheme, as every color is based on this hue.
     * Has values in [0, 360]. Its default value is [180], which is the hue of cyan.
     */
    var hue = 180
        set(value) {
            field = value
            updateAllColors()
        }

    private fun updateAllColors() {
        LightTheme.run {
            game91 = generateGame91Color(hue)
            game57 = generateGame57Color(hue)
            game29 = generateGame29Color(hue)
        }
        DarkTheme.run {
            game20 = generateGame20Color(hue)
            game95 = generateGame95Color(hue)
        }
    }

    object LightTheme {
        var game91 = generateGame91Color(hue)
        var game57 = generateGame57Color(hue)
        var game29 = generateGame29Color(hue)

        fun generateGame91Color(hue: Int) =
            Color().fromHsv(hue.toFloat(), 10f / 100f, 91f / 100f).apply { a = 1f }!!

        fun generateGame57Color(hue: Int) =
            Color().fromHsv(hue.toFloat(), 42f / 100f, 57f / 100f).apply { a = 1f }!!

        fun generateGame29Color(hue: Int) =
            Color().fromHsv(hue.toFloat(), 55f / 100f, 29f / 100f).apply { a = 1f }!!
    }

    object DarkTheme {
        var game20 = generateGame20Color(hue)
        var game95 = generateGame95Color(hue)

        fun generateGame20Color(hue: Int) =
            Color().fromHsv(hue.toFloat(), 94f / 100f, 20f / 100f).apply { a = 1f }!!

        fun generateGame95Color(hue: Int) =
            Color().fromHsv(hue.toFloat(), 85f / 100f, 95f / 100f).apply { a = 1f }!!
    }
}