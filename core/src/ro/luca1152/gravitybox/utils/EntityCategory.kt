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

/**
 * Used for Box2D collision detection.
 * Every Box2D body stores its own bits (categoryBits) and the bits of the bodies it can collide with (maskBits).
 */
enum class EntityCategory(bits: Int) {
    NONE(0x0000),
    FINISH(0x0001),
    PLAYER(0x0002),
    OBSTACLE(0x0003),
    BULLET(0x0004);

    var bits: Short = bits.toShort()
}