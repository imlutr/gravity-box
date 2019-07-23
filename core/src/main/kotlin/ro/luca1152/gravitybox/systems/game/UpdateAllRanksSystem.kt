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
import ro.luca1152.gravitybox.utils.leaderboards.ShotsLeaderboard

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
            val rankPercentage = calculateRankPercentage(i)
            if (rank != -1 && rankPercentage != -1f) {
                gameRules.run {
                    setGameLevelRank(i, rank)
                    setGameLevelRankPercentage(i, rankPercentage)
                }
            }
        }
    }

    private fun calculateRank(levelId: Int): Int {
        val shotsLeaderboard: GameShotsLeaderboard? = context.injectNullable()
        return if (shotsLeaderboard != null && shotsLeaderboard.levels.contains(ShotsLeaderboard.levelsKeys.getValue(levelId))) {
            var newRank = -1
            val shotsMap = shotsLeaderboard.levels[ShotsLeaderboard.levelsKeys.getValue(levelId)]!!.shots
            val shots = gameRules.getGameLevelHighscore(levelId)
            for (i in 1..shots) {
                if (shotsMap.containsKey(ShotsLeaderboard.shotsKeys(i)) && shotsMap[ShotsLeaderboard.shotsKeys(i)] != 0L) {
                    if (newRank == -1) newRank = 1
                    else newRank++
                }
            }
            if (newRank != -1) newRank else 1
        } else -1
    }

    private fun calculateRankPercentage(levelId: Int): Float {
        val shotsLeaderboard: GameShotsLeaderboard = context.inject()
        return if (shotsLeaderboard.levels.contains(ShotsLeaderboard.levelsKeys.getValue(levelId))) {
            val shotsMap = shotsLeaderboard.levels[ShotsLeaderboard.levelsKeys.getValue(levelId)]!!.shots
            val shots = gameRules.getGameLevelHighscore(levelId)
            var totalPlayers = 0L
            var totalPlayersWhoFinishedInFewerOrEqualShots = 0L
            shotsMap.forEach {
                totalPlayers += it.value
                if (it.key.substring(1).toLong() <= shots) {
                    totalPlayersWhoFinishedInFewerOrEqualShots += it.value
                }
            }
            if (totalPlayers != 0L) totalPlayersWhoFinishedInFewerOrEqualShots * 100f / totalPlayers else -1f
        } else -1f
    }
}