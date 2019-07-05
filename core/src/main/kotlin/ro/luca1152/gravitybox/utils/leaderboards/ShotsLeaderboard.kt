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
import pl.mk5.gdx.fireapp.GdxFIRAuth
import pl.mk5.gdx.fireapp.GdxFIRCrash
import pl.mk5.gdx.fireapp.GdxFIRDatabase
import pl.mk5.gdx.fireapp.auth.GdxFirebaseUser
import ro.luca1152.gravitybox.GameRules

class ShotsLeaderboard(context: Context) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun incrementPlayerCountForShotsBy(level: Int, shots: Int, increment: Int) {
        if (!gameRules.IS_MOBILE) {
            return
        }
        val databasePath = "shots-leaderboard/game/${gameRules.GAME_LEVELS_VERSION}/l$level/s$shots"
        GdxFIRAuth.inst().signInAnonymously().then<GdxFirebaseUser> {
            GdxFIRCrash.inst().log("Signing in anonymously into Firebase")
            GdxFIRDatabase.inst().inReference(databasePath).readValue(java.lang.Long::class.java).then<java.lang.Long> {
                if (it == null) {
                    GdxFIRCrash.inst().log("Setting the value at $databasePath to 0")
                    GdxFIRDatabase.inst().inReference(databasePath).setValue(0)
                }
            }.then<java.lang.Long> {
                GdxFIRDatabase.inst().inReference(databasePath).transaction(java.lang.Long::class.java) { value ->
                    GdxFIRCrash.inst()
                        .log("Incrementing the value at $databasePath by $increment ($value becomes ${value.toLong() + increment})")
                    (value.toLong() + increment) as java.lang.Long
                }
            }.then<java.lang.Long> {
                if (increment < 0 && (it != null && it.toLong() + increment <= 0)) {
                    GdxFIRCrash.inst().log("Removing the value at $databasePath")
                    GdxFIRDatabase.inst().inReference(databasePath).removeValue()
                }
            }
        }
    }

    fun incrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, 1)
    }

    fun decrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, -1)
    }
}