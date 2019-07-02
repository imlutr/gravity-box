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

import ktx.inject.Context
import pl.mk5.gdx.fireapp.GdxFIRDatabase
import ro.luca1152.gravitybox.GameRules

class ShotsLeaderboard(context: Context) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    private fun getIntFromString(string: String) = Integer.parseInt(string)

    private fun incrementPlayerCountForShotsBy(level: Int, shots: Int, increment: Int) {
        if (!gameRules.IS_MOBILE) {
            return
        }
        val databasePath = "shots-leaderboard/game/l$level/s$shots"
        GdxFIRDatabase.inst()
            // Update the player count for the given number of shots
            .inReference(databasePath).transaction(String::class.java) { "${getIntFromString(it) + increment}" }
            .then(GdxFIRDatabase.inst().inReference(databasePath).readValue(String::class.java))
            // If the player count is 0 (or, for some reason, negative), delete the entry
            .then<String> {
                if (increment < 0 && getIntFromString(it) <= 0) {
                    GdxFIRDatabase.inst().inReference(databasePath).removeValue()
                }
                // Do all of the above only after you made sure there is already a value
            }.after(
                GdxFIRDatabase.inst().inReference(databasePath).readValue(String::class.java).then<String> {
                    if (it == null || it == "") {
                        GdxFIRDatabase.inst().inReference(databasePath).setValue("1")
                    }
                }
            )
            .silentFail()
    }

    fun incrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, 1)
    }

    fun decrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, -1)
    }
}