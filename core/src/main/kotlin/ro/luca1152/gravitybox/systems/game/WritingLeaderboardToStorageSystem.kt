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
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import kotlin.concurrent.thread

class WriteLeaderboardToStorageEvent : Event
class WritingLeaderboardToStorageSystem(private val context: Context) :
    EventSystem<WriteLeaderboardToStorageEvent>(context.inject(), WriteLeaderboardToStorageEvent::class) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    override fun processEvent(event: WriteLeaderboardToStorageEvent, deltaTime: Float) {
        // The leaderboard shouldn't be null, but if it is, for some reason, better avoid a NullPointerException
        if (context.injectNullable<GameShotsLeaderboard>() == null) return

        writeLeaderboardToStorage()
    }

    private fun writeLeaderboardToStorage() {
        thread {
            val file = Gdx.files.local(Assets.gameLeaderboardPath)
            file.writeBytes(Json().prettyPrint(context.inject<GameShotsLeaderboard>()).toByteArray(), false)
            gameRules.run {
                CACHED_LEADERBOARD_VERSION = gameRules.GAME_LEVELS_VERSION
                flushUpdates()
            }
        }
    }
}