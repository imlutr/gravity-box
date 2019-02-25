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

package ro.luca1152.gravitybox.utils.box2d

/**
 * Used for Box2D collision detection.
 * Every Box2D body stores its own bits (categoryBits) and the bits of the bodies it can collide with (maskBits).
 */
enum class EntityCategory(bits: Int) {
    NONE(0b00000),
    FINISH(0b00001),
    POINT(0b00001),
    PLAYER(0b0010),
    BULLET(0b00100),
    PLATFORM(0b01000),
    OBSTACLE(0b01110);

    val bits: Short = bits.toShort()
}
