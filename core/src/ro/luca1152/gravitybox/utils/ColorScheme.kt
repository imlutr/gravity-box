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
    // The hue of the first level
    private const val initialHue = 180

    // The 4 colors used in the game
    var lightColor = getLightColor(initialHue)
    var darkColor = getDarkColor(initialHue)
    var lightColor2 = getLightColor2(initialHue)
    var darkColor2 = getDarkColor2(initialHue)

    // Methods to get the 4 colors from a given [hue]
    fun getLightColor(hue: Int) = Color().fromHsv(hue.toFloat(), 10f / 100f, 91f / 100f).apply { a = 1f }!!
    fun getDarkColor(hue: Int) = Color().fromHsv(hue.toFloat(), 42f / 100f, 57f / 100f).apply { a = 1f }!!
    fun getLightColor2(hue: Int) = Color().fromHsv(hue.toFloat(), 94f / 100f, 20f / 100f).apply { a = 1f }!!
    fun getDarkColor2(hue: Int) = Color().fromHsv(hue.toFloat(), 85f / 100f, 95f / 100f).apply { a = 1f }!!
}