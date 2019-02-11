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
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.tryGet

/**
 * Sync the position of the PhysicsComponent's Box2D body with other components.
 */
class PhysicsSyncSystem : IteratingSystem(
    Family.all(PhysicsComponent::class.java).one(
        ImageComponent::class.java,
        CollisionBoxComponent::class.java
    ).get()
) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.physics.body

        // Sync the Image's position
        entity.tryGet(ImageComponent)?.let {
            entity.image.setPosition(body.worldCenter.x, body.worldCenter.y)
            entity.image.img.rotation = body.angle * MathUtils.radDeg
        }


        // Sync the CollisionBox's position
        entity.tryGet(CollisionBoxComponent)?.let {
            entity.collisionBox.box.setPosition(
                body.worldCenter.x - entity.collisionBox.size / 2f,
                body.worldCenter.y - entity.collisionBox.size / 2f
            )
        }
    }
}