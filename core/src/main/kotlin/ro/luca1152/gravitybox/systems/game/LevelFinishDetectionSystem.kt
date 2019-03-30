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
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

/** Detects when the player is inside the finish point. */
class LevelFinishDetectionSystem : EntitySystem() {
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity
    private val playerIsInsideFinishPoint
        get() = playerEntity.collisionBox.box.overlaps(finishEntity.collisionBox.box)

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
        finishEntity = engine.getSingleton<FinishComponent>()
    }

    override fun update(deltaTime: Float) {
        updateVariables()
    }

    private fun updateVariables() {
        when (playerIsInsideFinishPoint) {
            true -> {
                playerEntity.player.isInsideFinishPoint = true
            }
            else -> {
                playerEntity.player.isInsideFinishPoint = false
            }
        }
    }
}