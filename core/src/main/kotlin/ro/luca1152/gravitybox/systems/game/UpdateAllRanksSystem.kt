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

package ro.luca1152.gravitybox.systems.game

import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.utils.kotlin.info
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard

class UpdateAllRanksEvent : Event
class UpdateAllRanksSystem(private val context: Context) : EventSystem<UpdateAllRanksEvent>(context.inject(), UpdateAllRanksEvent::class) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    override fun processEvent(event: UpdateAllRanksEvent, deltaTime: Float) {
        updateAllRanks()
        info("Updated all ranks.")
    }

    private fun updateAllRanks() {
        for (i in 1..gameRules.HIGHEST_FINISHED_LEVEL) {
            val rank = calculateRank(i)
            if (rank != -1) {
                gameRules.setGameLevelRank(i, rank)
            }
        }
    }

    private fun calculateRank(levelId: Int): Int {
        val shotsLeaderboard: GameShotsLeaderboard? = context.injectNullable()
        if (shotsLeaderboard != null && shotsLeaderboard.levels.contains("l$levelId")) {
            var newRank = -1
            val shotsMap = shotsLeaderboard.levels["l$levelId"]!!.shots
            val shots = gameRules.getGameLevelHighscore(levelId)
            for (i in 1..shots) {
                if (shotsMap.containsKey("s$i") && shotsMap["s$i"] != 0L) {
                    if (newRank == -1) newRank = 1
                    else newRank++
                }
            }
            return if (newRank != -1) newRank else 1
        } else return -1
    }
}