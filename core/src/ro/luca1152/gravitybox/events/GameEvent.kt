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

package ro.luca1152.gravitybox.events

enum class GameEvent {
    // General events
    LEVEL_FINISHED, // The player stayed long enough in the finish point so the level is finished
    LEVEL_RESTART, // The player is off-screen

    // Collisions
    BULLET_PLATFORM_COLLISION, // Collision between a bullet and a platform
    PLAYER_FINISH_COLLISION, // Collision between the player and the finish point
}