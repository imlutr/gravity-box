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

package ro.luca1152.gravitybox.utils.leaderboards

open class ShotsLeaderboard {
    var levels = mutableMapOf<String, Level>()
}

class Level {
    companion object {
        // Levels
        val levelsFilePath = (1..269).associateWith { "leaderboards/game/l$it" }
        val levelsFilePathToInt = (1..269).associateBy { "leaderboards/game/l$it" }
        val levelsKeys = (1..269).associateWith { "l$it" }

        // Shots
        private val shotsKeys = (1..1000).associateWith { "s$it" }
        private val shotsKeysToInt = (1..1000).associateBy { "s$it" }
        fun shotsKeys(intShots: Int): String {
            return if (shotsKeys.containsKey(intShots)) shotsKeys.getValue(intShots)
            else "s$intShots"

        }

        fun shotsKeysToInt(shotsKey: String): Int {
            return if (shotsKeysToInt.containsKey(shotsKey)) shotsKeysToInt.getValue(shotsKey)
            else shotsKey.substring(1).toInt()
        }
    }

    var shots = mutableMapOf<String, Long>()
}