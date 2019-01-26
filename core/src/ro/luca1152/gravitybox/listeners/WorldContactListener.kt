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

package ro.luca1152.gravitybox.listeners

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.physics.box2d.*
import ro.luca1152.gravitybox.components.BulletComponent
import ro.luca1152.gravitybox.components.PlatformComponent
import ro.luca1152.gravitybox.components.bullet
import ro.luca1152.gravitybox.components.platform
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import ro.luca1152.gravitybox.components.utils.tryGet
import ro.luca1152.gravitybox.events.GameEvent
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Dispatches the appropriate events for every Box2D collisions.
 */
class WorldContactListener(
    private val gameEventSignal: Signal<GameEvent>,
    private val world: World = Injekt.get(),
    private val engine: PooledEngine = Injekt.get()
) : ContactListener {
    /**
     * Called automatically when two Box2D bodies collide.
     */
    override fun beginContact(contact: Contact) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        // The collision isn't between two entities
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

    // --------------- Unused ContactListener functions ---------------
    override fun endContact(contact: Contact?) {}

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}
}

/**
 * Returns which of [entityA] and [entityB] has the [componentResolver] component.
 * If none, returns null.
 */
fun <T : Component> findEntity(componentResolver: ComponentResolver<T>, entityA: Entity, entityB: Entity) =
    when {
        entityA.tryGet(componentResolver) != null -> entityA
        entityB.tryGet(componentResolver) != null -> entityB
        else -> null
    }