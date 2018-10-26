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

object ColorScheme {
    // The colors from the color scheme
    private var lightColor = Color()
    private var darkColor = Color()
    private var lightColor2 = Color()
    private var darkColor2 = Color()

    /** If true, the darker color scheme will be used */
    var useDarkColorScheme = false
    /** Stores the current light color. Used in transitions between the two color schemes. */
    var currentLightColor = Color()
    /** Stores the current dark color. Used in transitions between the two color schemes. */
    var currentDarkColor = Color()
    /** Stores the target light color towards which currentLightColor must transition. */
    val currentLightLerpColor
        get() = if (useDarkColorScheme) lightColor2 else lightColor
    /** Stores the target dark color towards which currentLightColor must transition. */
    val currentDarkLerpColor
        get() = if (useDarkColorScheme) darkColor2 else darkColor

    init {
        // Load the initial colors
        updateColors(180) // 180 is the hue of the first level
    }

    fun updateColors(hue: Int) {
        // Functions to generate the color scheme's colors based on a hue (1-360)
        fun getLightColor(hue: Int) = Color().fromHsv(hue.toFloat(), 10f / 100f, 91f / 100f).apply { a = 1f }!!
        fun getDarkColor(hue: Int) = Color().fromHsv(hue.toFloat(), 42f / 100f, 57f / 100f).apply { a = 1f }!!
        fun getLightColor2(hue: Int) = Color().fromHsv(hue.toFloat(), 94f / 100f, 20f / 100f).apply { a = 1f }!!
        fun getDarkColor2(hue: Int) = Color().fromHsv(hue.toFloat(), 85f / 100f, 95f / 100f).apply { a = 1f }!!

        // Store the new colors
        lightColor = getLightColor(hue)
        darkColor = getDarkColor(hue)
        lightColor2 = getLightColor2(hue)
        darkColor2 = getDarkColor2(hue)

        // Update the current colors too because this function is called when changing the levels
        // and there is no transition between levels' colors, only when the player enters/leaves the finish point.
        currentLightColor = lightColor.cpy()
        currentDarkColor = darkColor.cpy()
    }
}