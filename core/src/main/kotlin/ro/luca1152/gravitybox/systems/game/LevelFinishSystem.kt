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
import com.badlogic.ashley.core.EntitySystem
import ktx.inject.Context
import pl.mk5.gdx.fireapp.GdxFIRAnalytics
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.info
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboardController
import kotlin.math.max

/** Handles what happens when a level is finished. */
class LevelFinishSystem(
    private val context: Context,
    private val restartLevelWhenFinished: Boolean = false
) : EntitySystem() {
    // Injected objects
    private val eventQueue: EventQueue = context.inject()
    private val gameRules: GameRules = context.inject()
    private val gameShotsLeaderboardController: GameShotsLeaderboardController = context.inject()

    // Entities
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    // Variables
    private var didLogLevelFinish = false
    private var didWriteRankToStorage = false
    private var didUpdateLeaderboard = false

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelEntity.level.isLevelFinished || levelEntity.level.isRestarting) {
            didWriteRankToStorage = false
            didLogLevelFinish = false
            didUpdateLeaderboard = false
            return
        }

        if (!didWriteRankToStorage) {
            eventQueue.add(WriteRankToStorageEvent())
            didWriteRankToStorage = true
        }

        if (!didLogLevelFinish) {
            logLevelFinish()
            didLogLevelFinish = true
        }

        if (!didUpdateLeaderboard) {
            updateLeaderboard()
            didUpdateLeaderboard = true
        }

        if (restartLevelWhenFinished) {
            levelEntity.level.restartLevel = true
            return
        }

        // The leaderboard wasn't loaded yet, showing the finish UI is pointless
        if (context.injectNullable<GameShotsLeaderboard>() == null) {
            eventQueue.add(ShowNextLevelEvent())
        } else {
            // Make sure the rank is calculated if the leaderboard was just loaded
            eventQueue.add(CalculateRankEvent())
        }

        updateHighestFinishedLevel()
    }

    private fun logLevelFinish() {
        gameRules.run {
            setGameLevelFinishCount(levelEntity.level.levelId, gameRules.getGameLevelFinishCount(levelEntity.level.levelId) + 1)
            flushUpdates()
        }

        // Analytics
        if (gameRules.IS_MOBILE) {
            GdxFIRAnalytics.inst().logEvent(
                "level_finish",
                mapOf(
                    Pair("level_id", "game/${levelEntity.level.levelId}"),
                    Pair("finish_time", "${gameRules.getGameLevelPlayTime(levelEntity.level.levelId)}"),
                    Pair("finish_count", "${gameRules.getGameLevelFinishCount(levelEntity.level.levelId)}")
                )
            )
        }

        info(
            "Logged level finish (level ${levelEntity.level.levelId}, ${"%.2f".format(gameRules.getGameLevelPlayTime(levelEntity.level.levelId))}s," +
                    " ${gameRules.getGameLevelFinishCount(levelEntity.level.levelId)} time" +
                    "${if (gameRules.getGameLevelFinishCount(levelEntity.level.levelId) != 1) "s" else ""})."
        )

        // Reset the played time, so in case this level is replayed, a huge time won't be reported
        gameRules.setGameLevelPlayTime(levelEntity.level.levelId, 0f)
    }

    private fun updateLeaderboard() {
        if (gameRules.IS_PLAYER_SOFT_BANNED || levelEntity.map.doShotsAndEncryptedShotsDiffer()) {
            gameRules.setGameLevelHighscore(levelEntity.level.levelId, levelEntity.map.shots)
            return
        }

        val shots = levelEntity.map.shots
        levelEntity.level.run {
            if (gameRules.getGameLevelHighscore(levelId) <= shots)
                return

            gameShotsLeaderboardController.incrementPlayerCountForShots(levelId, shots)
            if (gameRules.getGameLevelHighscore(levelId) != gameRules.DEFAULT_HIGHSCORE_VALUE &&
                gameRules.getGameLevelHighscore(levelId) != gameRules.SKIPPED_LEVEL_SCORE_VALUE
            ) {
                gameShotsLeaderboardController.decrementPlayerCountForShots(levelId, gameRules.getGameLevelHighscore(levelId))
            }
            gameRules.setGameLevelHighscore(levelId, shots)
        }
    }


    private fun updateHighestFinishedLevel() {
        gameRules.HIGHEST_FINISHED_LEVEL = max(gameRules.HIGHEST_FINISHED_LEVEL, levelEntity.level.levelId)
    }
}