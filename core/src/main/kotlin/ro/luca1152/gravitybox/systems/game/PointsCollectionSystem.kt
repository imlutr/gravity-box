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
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Handles what happens when the player collides with a collectible point. */
class PointsCollectionSystem(context: Context) : IteratingSystem(Family.all(CollectiblePointComponent::class.java).get()) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    // Entities
    private lateinit var playerEntity: Entity
    private lateinit var levelEntity: Entity

    private val Entity.collidesWithPlayer: Boolean
        get() {
            playerEntity.polygon.update()
            this.polygon.update()
            return this.polygon.polygon.boundingRectangle.overlaps(playerEntity.polygon.polygon.boundingRectangle)
        }

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        playerEntity = engine.getSingleton<PlayerComponent>()
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.collidesWithPlayer) {
            collectPoint(entity)
        }
    }

    private fun collectPoint(entity: Entity) {
        if ((entity.tryGet(EditorObjectComponent) == null || !entity.editorObject.isDeleted) && !entity.collectiblePoint.isCollected) {
            entity.run {
                collectiblePoint.isCollected = true
                scene2D.isVisible = false
            }
            levelEntity.map.collectedPointsCount++
            gameRules.COLLECTED_POINT_COUNT++
        }
    }
}