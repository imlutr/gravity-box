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
import ro.luca1152.gravitybox.events.CacheCurrentLevelLeaderboardEvent
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.events.WriteLeaderboardToStorageEvent
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboardController

class CurrentLevelLeaderboardCachingSystem(private val context: Context) :
    EventSystem<CacheCurrentLevelLeaderboardEvent>(context.inject(), CacheCurrentLevelLeaderboardEvent::class) {
    // Injected objects
    private val shotsLeaderboardController: GameShotsLeaderboardController = context.inject()
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEvent(event: CacheCurrentLevelLeaderboardEvent, deltaTime: Float) {
        cacheCurrentLevelLeaderboard()
    }

    private fun cacheCurrentLevelLeaderboard() {
        val shotsLeaderboard = context.injectNullable<GameShotsLeaderboard>()
        shotsLeaderboardController.readCurrentGameLevelLeaderboard(levelEntity.level.levelId) {
            if (shotsLeaderboard == null) {
                val newShotsLeaderboard = GameShotsLeaderboard().apply {
                    levels["l${levelEntity.level.levelId}"] = it
                }
                if (!context.contains<GameShotsLeaderboard>()) {
                    context.bindSingleton(newShotsLeaderboard)
                }
            } else {
                shotsLeaderboard.levels["l${levelEntity.level.levelId}"] = it
            }
            eventQueue.add(WriteLeaderboardToStorageEvent())
        }
    }
}