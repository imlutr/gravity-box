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

package ro.luca1152.gravitybox.utils.box2d

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import ro.luca1152.gravitybox.components.game.BulletComponent
import ro.luca1152.gravitybox.components.game.PlatformComponent
import ro.luca1152.gravitybox.components.game.bullet
import ro.luca1152.gravitybox.components.game.platform
import ro.luca1152.gravitybox.utils.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Reacts accordingly to every Box2D collision. */
class WorldContactListener : ContactListener {
    override fun beginContact(contact: Contact) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        if (bodyA.userData !is Entity || bodyB.userData !is Entity)
            return

        // Get the entities from the bodies
        val entityA = bodyA.userData as Entity
        val entityB = bodyB.userData as Entity

        // Find the specific entities
        val bulletEntity = findEntity(BulletComponent, entityA, entityB)
        val platformEntity = findEntity(PlatformComponent, entityA, entityB)

        // A bullet and a platform collided
        if (bulletEntity != null && platformEntity != null) {
            // Remove the bullet
            bulletEntity.bullet.collidedWithPlatform = true
            bulletEntity.bullet.collidedWith = platformEntity

            // Remove the platform if it's dynamic
            if (platformEntity.platform.isDynamic)
                platformEntity.platform.remove = true
        }
    }

    override fun endContact(contact: Contact?) {}

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}
}

/**
 * Returns which of [entityA] and [entityB] has the [componentResolver] component.
 * If none, returns null.
 */
fun <T : Component> findEntity(componentResolver: ComponentResolver<T>, entityA: Entity, entityB: Entity) = when {
    entityA.tryGet(componentResolver) != null -> entityA
    entityB.tryGet(componentResolver) != null -> entityB
    else -> null
}