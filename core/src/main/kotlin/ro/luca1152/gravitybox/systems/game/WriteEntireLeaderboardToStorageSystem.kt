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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.utils.kotlin.info
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.leaderboards.Level
import kotlin.concurrent.thread

class WriteEntireLeaderboardToStorageEvent : Event
class WriteEntireLeaderboardToStorageSystem(private val context: Context) :
    EventSystem<WriteEntireLeaderboardToStorageEvent>(context.inject(), WriteEntireLeaderboardToStorageEvent::class) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val eventQueue: EventQueue = context.inject()

    override fun processEvent(event: WriteEntireLeaderboardToStorageEvent, deltaTime: Float) {
        if (context.injectNullable<GameShotsLeaderboard>() == null) {
            return
        }
        writeLeaderboardToStorage()
    }

    private fun writeLeaderboardToStorage() {
        thread {
            val leaderboard = context.inject<GameShotsLeaderboard>()
            for (i in 1 until gameRules.LEVEL_COUNT) {
                val level = leaderboard.levels[Level.levelsKeys.getValue(i)]
                val file = Gdx.files.local(Level.levelsFilePath.getValue(i))
                file.writeString(Json().toJson(level), false)
            }
            gameRules.CACHED_LEADERBOARD_VERSION = gameRules.GAME_LEVELS_VERSION
            eventQueue.add(FlushPreferencesEvent())
            info("Wrote the leaderboard to storage.")
        }
    }
}