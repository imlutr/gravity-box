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

package ro.luca1152.gravitybox.utils.assets.json

import ro.luca1152.gravitybox.components.game.MovingObjectComponent
import ro.luca1152.gravitybox.components.game.metersToPixels

class PaddingPrototype {
    var left = 0
    var right = 0
    var top = 0
    var bottom = 0
}

class PositionPrototype {
    var x = Float.POSITIVE_INFINITY
    var y = Float.POSITIVE_INFINITY
}

class PlayerPrototype {
    var position = PositionPrototype()
    var rotation = 0
}

class FinishPrototype {
    var position = PositionPrototype()
    var rotation = 0
}

class MovingToPrototype {
    var x = Float.POSITIVE_INFINITY
    var y = Float.POSITIVE_INFINITY
}

class ObjectPrototype {
    // Any map object
    var type = ""
    var position = PositionPrototype()
    var rotation = 0

    // Platform
    var width = 0f
    var isDestroyable = false
    var isRotating = false

    // Moving platform
    var movingTo = MovingToPrototype()
    var speed = MovingObjectComponent.SPEED.metersToPixels

    // Text
    var string = ""

    // Dashed line
    var start = PositionPrototype()
    var end = PositionPrototype()
}