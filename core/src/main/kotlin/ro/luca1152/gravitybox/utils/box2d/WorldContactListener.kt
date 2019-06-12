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
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.removeComponent
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Reacts accordingly to every Box2D collision. */
class WorldContactListener(private val context: Context) : ContactListener {
    private val engine: PooledEngine = context.inject()

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
        val destroyablePlatformEntity = findEntity(DestroyablePlatformComponent, entityA, entityB)
        val combinedPlatformEntity = findEntity(CombinedBodyComponent, entityA, entityB)
        val movingPlatformEntity = findEntity(MovingObjectComponent, entityA, entityB)
        val playerEntity = findEntity(PlayerComponent, entityA, entityB)

        // The bullet didn't already collide with another platform
        if (bulletEntity != null && !bulletEntity.bullet.collidedWithPlatform) {
            // A bullet and a platform collided
            if (platformEntity != null || destroyablePlatformEntity != null) {
                // The bullet collided with a destroyable platform that was hit by another bullet as well
                // So the bullet should simply be removed, without applying a blast impulse
                if (destroyablePlatformEntity != null && destroyablePlatformEntity.destroyablePlatform.remove) {
                    engine.removeEntity(bulletEntity)
                } else {
                    // Remove the bullet
                    bulletEntity.bullet.collidedWithPlatform = true
                    bulletEntity.bullet.collidedWith = platformEntity ?: destroyablePlatformEntity

                    if (destroyablePlatformEntity != null) {
                        destroyablePlatformEntity.destroyablePlatform.remove = true
                    }
                }
            }

            // A bullet collided a combined platform
            if (combinedPlatformEntity != null) {
                bulletEntity.bullet.collidedWithPlatform = true
                bulletEntity.bullet.collidedWith = combinedPlatformEntity
            }
        }

        if (playerEntity != null && movingPlatformEntity != null) {
            if (Math.abs(movingPlatformEntity.body.body!!.transform.rotation * MathUtils.radiansToDegrees) <= 45f &&
                playerEntity.scene2D.centerY >= movingPlatformEntity.polygon.bottommostY
            ) {
                playerEntity.passenger(context, movingPlatformEntity)
            }
        }
    }

    override fun endContact(contact: Contact) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        if (bodyA.userData !is Entity || bodyB.userData !is Entity)
            return

        // Get the entities from the bodies
        val entityA = bodyA.userData as Entity
        val entityB = bodyB.userData as Entity

        // Find the specific entities
        val movingPlatformEntity = findEntity(MovingObjectComponent, entityA, entityB)
        val playerEntity = findEntity(PlayerComponent, entityA, entityB)

        if (playerEntity != null && movingPlatformEntity != null && playerEntity.tryGet(PassengerComponent) != null) {
            playerEntity.removeComponent<PassengerComponent>()
        }
    }

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