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
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.events.GameEvent

/**
 * Dispatches the appropriate events for every Box2D collisions.
 */
class WorldContactListener(private val gameEventSignal: Signal<GameEvent>) : ContactListener {
    /**
     * Returns which of [entityA] and [entityB] has the [componentResolver] component.
     * If none, returns null.
     */
    private fun <T : Component> findEntity(componentResolver: ComponentResolver<T>, entityA: Entity, entityB: Entity): Entity? {
        if (entityA.tryGet(componentResolver) != null) return entityA
        if (entityB.tryGet(componentResolver) != null) return entityB
        return null
    }

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
            bulletEntity.bullet.collidedWithWall = true
            gameEventSignal.dispatch(GameEvent.BULLET_PLATFORM_COLLISION)
        }
    }

    // --------------- Unused ContactListener functions ---------------
    override fun endContact(contact: Contact?) {}

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}
}