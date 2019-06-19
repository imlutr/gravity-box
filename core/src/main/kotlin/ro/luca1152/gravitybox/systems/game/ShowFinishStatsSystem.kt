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
import com.badlogic.gdx.math.MathUtils
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.entities.game.TextEntity
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

/** Creates a TextEntity at the last level, containing game stats. */
class ShowFinishStatsSystem(private val context: Context) : EntitySystem() {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    // Entities
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    private val isPlayingLastLevel
        get() = levelEntity.level.levelId == gameRules.LEVEL_COUNT
    private val didStoreFinishStats
        get() = gameRules.DID_FINISH_GAME
    private var addedTextEntity = false

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!isPlayingLastLevel) {
            addedTextEntity = false
            return
        }
        // The map is loading, which would cause the removal of all entities including the text entities added here
        if (levelEntity.level.loadMap) {
            return
        }
        addTextEntity()
    }

    private fun addTextEntity() {
        if (addedTextEntity || !didStoreFinishStats) return
        TextEntity.createEntity(
            context, """
            You did it! You finished the game in
            ${secondsToTimeString(gameRules.FINISH_TIME)}! :D
        """.trimIndent(), 96f, 480.574f
        )

        TextEntity.createEntity(
            context, """
            Here are some interesting stats:
            You shot ${gameRules.FINISH_BULLET_COUNT} time${if (gameRules.FINISH_BULLET_COUNT != 1) "s" else ""}.
            You restarted ${gameRules.FINISH_RESTART_COUNT} time${if (gameRules.FINISH_RESTART_COUNT != 1) "s" else ""}.
            You died ${gameRules.FINISH_DEATH_COUNT} time${if (gameRules.FINISH_DEATH_COUNT != 1) "s" else ""}.
            You destroyed ${gameRules.FINISH_DESTROYED_PLATFORM_COUNT} platform${if (gameRules.FINISH_DESTROYED_PLATFORM_COUNT != 1) "s" else ""}.
            You collected ${gameRules.FINISH_COLLECTED_POINT_COUNT} point${if (gameRules.FINISH_COLLECTED_POINT_COUNT != 1) "s" else ""}
            You skipped ${gameRules.FINISH_SKIPPED_LEVELS_COUNT} level${if (gameRules.FINISH_SKIPPED_LEVELS_COUNT != 1) "s" else ""}
        """.trimIndent(), 96f, 262.177f
        )

        TextEntity.createEntity(
            context, """
            Also, if you didn't yet rate the game, it
            would be awesome if you'd do it! <3
        """.trimIndent(), 96f, -26.466f
        )

        addedTextEntity = true
    }

    private fun secondsToTimeString(seconds: Float): String {
        val finishHours = MathUtils.floor(seconds / 3600f)
        val finishMinutes = MathUtils.floor((seconds % 3600) / 60f)
        val finishSeconds = MathUtils.floor(seconds % 60)
        return "${if (finishHours != 0) "${finishHours}h " else ""}${finishMinutes}m ${finishSeconds}s"
    }
}