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
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Handles when a level can be finished (only when all the points, if any, were collected) */
class CanFinishLevelSystem(private val context: Context) : EntitySystem() {
    private lateinit var levelEntity: Entity
    private lateinit var finishEntity: Entity
    private val levelHasCollectiblePoints
        get() = levelEntity.map.pointsCount > 0
    private val canFinishLevel
        get() = levelEntity.map.collectedPointsCount == levelEntity.map.pointsCount

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        finishEntity = engine.getSingleton<FinishComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelHasCollectiblePoints) return
        handleCollectiblePoints()
    }

    private fun handleCollectiblePoints() {
        // The level cannot be finished, but the level entity wasn't updated, as canFinish is true by default
        if (levelEntity.level.canFinish && !canFinishLevel) {
            if (finishEntity.tryGet(FadeInFadeOutComponent) != null) {
                finishEntity.remove(FadeInFadeOutComponent::class.java)
            }
            finishEntity.scene2D.color.a = FinishEntity.FINISH_BLOCKED_ALPHA
            levelEntity.level.canFinish = false
        } else if (!levelEntity.level.canFinish && canFinishLevel) {
            if (finishEntity.tryGet(FadeInFadeOutComponent) == null) {
                finishEntity.fadeInFadeOut(context, finishEntity.scene2D)
            }
            levelEntity.level.canFinish = true
        }
    }
}