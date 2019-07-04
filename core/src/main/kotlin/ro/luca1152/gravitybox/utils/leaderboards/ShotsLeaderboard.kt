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
import pl.mk5.gdx.fireapp.GdxFIRCrash
import pl.mk5.gdx.fireapp.GdxFIRDatabase
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.utils.kotlin.stringStackTrace

class ShotsLeaderboard(context: Context) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun incrementPlayerCountForShotsBy(level: Int, shots: Int, increment: Int) {
        if (!gameRules.IS_MOBILE) {
            return
        }
        val databasePath = "shots-leaderboard/game/${gameRules.GAME_LEVELS_VERSION}/l$level/s$shots"
        GdxFIRDatabase.inst()
            // Update the player count for the given number of shots
            .inReference(databasePath).transaction(java.lang.Long::class.java) { (it.toLong() + increment) as java.lang.Long }
            .fail { _, e ->
                GdxFIRCrash.inst().log("Couldn't increment the value at $databasePath. Exception:\n${e.stringStackTrace}")
            }
            .then(GdxFIRDatabase.inst().inReference(databasePath).readValue(java.lang.Long::class.java))
            .fail { _, e ->
                GdxFIRCrash.inst().log("Couldn't read the value at $databasePath. Exception:\n${e.stringStackTrace}")
            }
            // If the player count is 0 (or, for some reason, negative), delete the entry
            .then<java.lang.Long> {
                if (increment < 0 && it <= 0) {
                    GdxFIRDatabase.inst().inReference(databasePath).removeValue().fail { _, e ->
                        GdxFIRCrash.inst().log("Couldn't remove the value at $databasePath. Exception:\n${e.stringStackTrace}")
                    }
                }
                // Do all of the above only after you made sure that the databasePath really points to an entry; if it doesn't, create one
            }.after(
                GdxFIRDatabase.inst().inReference(databasePath).readValue(java.lang.Long::class.java).then<java.lang.Long> {
                    if (it == null) {
                        GdxFIRDatabase.inst().inReference(databasePath).setValue(1).fail { _, e ->
                            GdxFIRCrash.inst().log("Couldn't set the value at $databasePath to 1. Exception:\n${e.stringStackTrace}")
                        }
                    }
                }
            )
            .fail { _, e ->
                GdxFIRCrash.inst().log("Couldn't modify the value at $databasePath. Exception:\n${e.stringStackTrace}")
            }
    }

    fun incrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, 1)
    }

    fun decrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, -1)
    }
}