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
import ro.luca1152.gravitybox.events.Event
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.leaderboards.Level

class WriteLevelLeaderboardToStorageEvent(val levelId: Int) : Event
class WriteLevelLeaderboardToStorageSystem(
    private val context: Context
) : EventSystem<WriteLevelLeaderboardToStorageEvent>(context.inject(), WriteLevelLeaderboardToStorageEvent::class) {
    override fun processEvent(event: WriteLevelLeaderboardToStorageEvent, deltaTime: Float) {
        if (context.injectNullable<GameShotsLeaderboard>() == null) {
            return
        }
        writeLevelLeaderboardToStorage(event.levelId)
    }

    private fun writeLevelLeaderboardToStorage(levelId: Int) {
        // Injected objects
        val leaderboard: GameShotsLeaderboard = context.inject()

        val level = leaderboard.levels[Level.levelsKeys.getValue(levelId)]
        val file = Gdx.files.local(Level.levelsFilePath.getValue(levelId))
        file.writeString(Json().toJson(level), false)
    }
}