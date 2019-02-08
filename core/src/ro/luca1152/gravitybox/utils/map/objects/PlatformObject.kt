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

package ro.luca1152.gravitybox.utils.map.objects

import ro.luca1152.gravitybox.utils.map.MapObject

class PlatformObject(id: Int,
                     var type: PlatformType,
                     var x: Float = 0f, var y: Float = 0f,
                     var width: Float = DEFAULT_WIDTH,
                     var height: Float = DEFAULT_HEIGHT) : MapObject(id) {
    companion object {
        const val DEFAULT_WIDTH = 1f
        const val DEFAULT_HEIGHT = .25f
    }
}

enum class PlatformType {
    FULL, // Full interior, can be destroyed
    EMPTY // Empty interior, can be destroyed
}

enum class CornerType {
    ROUNDED,
    STRAIGHT
}