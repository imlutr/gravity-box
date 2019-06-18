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
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

/** Updates the appropriate stats when the player finished every level. */
class GameFinishSystem(context: Context) : EntitySystem() {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    private val isLastLevel
        get() = levelEntity.level.levelId == gameRules.LEVEL_COUNT
    private val alreadyStoredStats
        get() = gameRules.DID_FINISH_GAME

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!isLastLevel || alreadyStoredStats) return
        storeStats()
    }

    private fun storeStats() {
        gameRules.run {
            DID_FINISH_GAME = true
            FINISH_TIME = PLAY_TIME
            FINISH_BULLET_COUNT = BULLET_COUNT
            FINISH_RESTART_COUNT = RESTART_COUNT
            FINISH_DEATH_COUNT = DEATH_COUNT
            FINISH_DESTROYED_PLATFORM_COUNT = DESTROYED_PLATFORMS_COUNT
            FINISH_COLLECTED_POINT_COUNT = COLLECTED_POINT_COUNT
            FINISH_SKIPPED_LEVELS_COUNT = SKIPPED_LEVELS_COUNT
        }
    }
}