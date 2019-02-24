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
import com.badlogic.gdx.physics.box2d.BodyDef
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.tryGet

/** Syncs [BodyComponent] properties with other components. */
class PhysicsSyncSystem : IteratingSystem(Family.all(BodyComponent::class.java).one(ImageComponent::class.java, CollisionBoxComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.body.body.type == BodyDef.BodyType.StaticBody)
            return
        if (entity.tryGet(ImageComponent) != null)
            syncBodyPropertiesWithImage(entity, entity.image)
        if (entity.tryGet(CollisionBoxComponent) != null)
            syncBodyPositionWithCollisionBox(entity, entity.collisionBox)
    }

    private fun syncBodyPropertiesWithImage(physicsEntity: Entity, image: ImageComponent) {
        image.setPosition(physicsEntity.body.body.worldCenter.x, physicsEntity.body.body.worldCenter.y)
        image.img.rotation = physicsEntity.body.body.angle * MathUtils.radDeg
    }

    private fun syncBodyPositionWithCollisionBox(physicsEntity: Entity, collisionBox: CollisionBoxComponent) {
        collisionBox.box.setPosition(
                physicsEntity.body.body.worldCenter.x - collisionBox.size / 2f,
                physicsEntity.body.body.worldCenter.y - collisionBox.size / 2f
        )
    }
}