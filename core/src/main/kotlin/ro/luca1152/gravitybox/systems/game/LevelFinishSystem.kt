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
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard

/** Handles what happens when a level is finished. */
class LevelFinishSystem(
    private val context: Context,
    private val restartLevelWhenFinished: Boolean = false
) : EntitySystem() {
    // Injected objects
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    private var didWriteRankToStorage = false

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelEntity.level.isLevelFinished || levelEntity.level.isRestarting) {
            didWriteRankToStorage = false
            return
        }

        if (!didWriteRankToStorage) {
            println("plmmm write rank to storage")
            eventQueue.add(WriteRankToStorageEvent())
            didWriteRankToStorage = true
        }

        if (restartLevelWhenFinished) {
            levelEntity.level.restartLevel = true
            return
        }

        // The leaderboard wasn't loaded yet, showing the finish UI is pointless
        if (context.injectNullable<GameShotsLeaderboard>() == null) {
            eventQueue.add(ShowNextLevelEvent())
        }
    }
}