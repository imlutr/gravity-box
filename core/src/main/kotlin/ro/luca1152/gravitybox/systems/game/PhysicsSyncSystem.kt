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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Syncs [BodyComponent]'s properties with other components. */
class PhysicsSyncSystem : IteratingSystem(
    Family.all(BodyComponent::class.java).one(Scene2DComponent::class.java, CollisionBoxComponent::class.java).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.body.body == null) return
        if (entity.tryGet(EditorObjectComponent) != null && entity.editorObject.isDeleted) return

        if (entity.tryGet(Scene2DComponent) != null && entity.tryGet(CombinedBodyComponent) == null) {
            if (entity.tryGet(DestroyablePlatformComponent) == null || !entity.destroyablePlatform.isRemoved)
                syncBodyPropertiesWithScene2D(entity, entity.scene2D)
        }

        if (entity.tryGet(CollisionBoxComponent) != null) {
            syncBodyPositionWithCollisionBox(entity, entity.collisionBox)
        }
    }

    private fun syncBodyPropertiesWithScene2D(physicsEntity: Entity, scene2D: Scene2DComponent) {
        scene2D.run {
            centerX = physicsEntity.body.body!!.worldCenter.x
            centerY = physicsEntity.body.body!!.worldCenter.y
            rotation = physicsEntity.body.body!!.angle * MathUtils.radDeg
        }
    }

    private fun syncBodyPositionWithCollisionBox(physicsEntity: Entity, collisionBox: CollisionBoxComponent) {
        collisionBox.box.setPosition(
            physicsEntity.body.body!!.worldCenter.x - physicsEntity.collisionBox.width / 2f,
            physicsEntity.body.body!!.worldCenter.y - physicsEntity.collisionBox.height / 2f
        )
    }
}