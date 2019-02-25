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
import com.badlogic.ashley.core.Family
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.removeAndResetEntity
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor

/** Handles what happens when a level is marked as to be restarted. */
class LevelRestartSystem : EntitySystem() {
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        if (!levelEntity.level.restartLevel)
            return
        restartTheLevel()
    }

    private fun restartTheLevel() {
        engine.getEntitiesFor(Family.all(BodyComponent::class.java).get()).forEach {
            it.body.resetToInitialState()
        }
        engine.getEntitiesFor(Family.all(BulletComponent::class.java).get()).forEach {
            engine.removeAndResetEntity(it)
        }
        levelEntity.level.restartLevel = false
    }
}