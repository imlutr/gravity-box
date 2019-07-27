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
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.leaderboards.ShotsLeaderboard

class CalculateRankEvent : Event
class LeaderboardRankCalculationSystem(
    private val context: Context
) : EventSystem<CalculateRankEvent>(context.inject(), CalculateRankEvent::class) {
    // Injected objects
    private val eventQueue: EventQueue = context.inject()
    private val playScreen: PlayScreen = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEvent(event: CalculateRankEvent, deltaTime: Float) {
        val shotsLeaderboard: GameShotsLeaderboard? = context.injectNullable()
        if (shotsLeaderboard == null || !shotsLeaderboard.levels.contains(ShotsLeaderboard.levelsKeys.getValue(levelEntity.level.levelId))) {
            levelEntity.map.run {
                rank = -1
                rankPercentage = -1f
            }
            return
        }

        calculateRank()
        calculateRankPercentage()
        showLabel()
    }

    private fun calculateRank() {
        val shotsLeaderboard: GameShotsLeaderboard = context.inject()
        var newRank = -1
        val levelKey = ShotsLeaderboard.levelsKeys[levelEntity.level.levelId]
        val shotsMap = shotsLeaderboard.levels[levelKey]!!.shots
        for (i in 1..levelEntity.map.shots) {
            val shotKey = ShotsLeaderboard.shotsKeys(i)
            if (shotsMap.containsKey(shotKey) && shotsMap[shotKey] != 0L) {
                if (newRank == -1) newRank = 1
                else newRank++
            }
        }
        levelEntity.map.run {
            rank = newRank
            isNewRecord = rank == -1 && !shotsMap.containsKey(ShotsLeaderboard.shotsKeys(levelEntity.level.levelId))

            if (isNewRecord && levelEntity.level.isLevelFinished) {
                eventQueue.addScheduled(CacheCurrentLevelShots())
            }
        }
    }

    private fun calculateRankPercentage() {
        val shotsLeaderboard: GameShotsLeaderboard = context.inject()
        val levelKey = ShotsLeaderboard.levelsKeys[levelEntity.level.levelId]
        val shotsMap = shotsLeaderboard.levels[levelKey]!!.shots
        val shots = levelEntity.map.shots
        var totalPlayers = 0L
        var totalPlayersWhoFinishedInFewerOrEqualShots = 0L
        shotsMap.forEach {
            totalPlayers += it.value
            val intShots = ShotsLeaderboard.shotsKeysToInt((it.key))
            if (intShots <= shots) {
                totalPlayersWhoFinishedInFewerOrEqualShots += it.value
            }
        }
        if (totalPlayers != 0L) {
            levelEntity.map.rankPercentage = totalPlayersWhoFinishedInFewerOrEqualShots * 100f / totalPlayers
        }
    }

    private fun showLabel() {
        if (playScreen.rankLabel.color.a == 0f && !playScreen.rankLabel.hasActions() &&
            !levelEntity.level.isLevelFinished && levelEntity.map.rank != -1 && !levelEntity.level.isRestarting
        ) {
            playScreen.rankLabel.color.a = 1f
        }
    }
}