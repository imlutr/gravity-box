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

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard

class CalculateRankEvent : Event
class LeaderboardRankCalculationSystem(
    private val context: Context
) : EventSystem<CalculateRankEvent>(context.inject(), CalculateRankEvent::class) {
    // Injected objects
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEvent(event: CalculateRankEvent, deltaTime: Float) {
        calculateRank()
        calculateRankPercentage()
    }

    private fun calculateRank() {
        val shotsLeaderboard: GameShotsLeaderboard? = context.injectNullable()
        if (shotsLeaderboard == null || !shotsLeaderboard.levels.contains("l${levelEntity.level.levelId}")) {
            levelEntity.map.rank = -1
        } else {
            var newRank = -1
            val shotsMap = shotsLeaderboard.levels["l${levelEntity.level.levelId}"]!!.shots
            for (i in 1..levelEntity.map.shots) {
                if (shotsMap.containsKey("s$i") && shotsMap["s$i"] != 0L) {
                    if (newRank == -1) newRank = 1
                    else newRank++
                }
            }
            levelEntity.map.run {
                rank = newRank
                isNewRecord = !shotsMap.containsKey("s${levelEntity.map.shots}")

                if (isNewRecord && levelEntity.level.isLevelFinished) {
                    eventQueue.add(CacheCurrentLevelShots())
                }
            }
        }
    }

    private fun calculateRankPercentage() {
        val shotsLeaderboard: GameShotsLeaderboard? = context.injectNullable()
        if (shotsLeaderboard == null) {
            levelEntity.map.rankPercentage = -1f
        } else {
            val shotsMap = shotsLeaderboard.levels["l${levelEntity.level.levelId}"]!!.shots
            val shots = levelEntity.map.shots
            var totalPlayers = 0L
            var totalPlayersWhoFinishedInFewerOrEqualShots = 0L
            shotsMap.forEach {
                totalPlayers += it.value
                if (it.key.substring(1).toLong() <= shots) {
                    totalPlayersWhoFinishedInFewerOrEqualShots += it.value
                }
            }
            if (totalPlayers != 0L) {
                levelEntity.map.rankPercentage = totalPlayersWhoFinishedInFewerOrEqualShots * 100f / totalPlayers
            }
        }
    }
}