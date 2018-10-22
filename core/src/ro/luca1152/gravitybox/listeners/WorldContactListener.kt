package ro.luca1152.gravitybox.listeners

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.PlatformComponent
import ro.luca1152.gravitybox.components.PlayerComponent
import ro.luca1152.gravitybox.components.tryGet
import ro.luca1152.gravitybox.events.GameEvent

class WorldContactListener(private val gameEventSignal: Signal<GameEvent>) : ContactListener {
    private fun <T : Component> findEntity(componentResolver: ComponentResolver<T>, entityA: Entity, entityB: Entity): Entity? {
        if (entityA.tryGet(componentResolver) != null) return entityA
        if (entityB.tryGet(componentResolver) != null) return entityB
        return null
    }

    override fun beginContact(contact: Contact) {
        val bodyA = contact.fixtureA.body
        val bodyB = contact.fixtureB.body

        // The collision isn't between two entities
        if (bodyA.userData !is Entity || bodyB.userData !is Entity)
            return

        // Get the entities from the bodies
        val entityA = bodyA.userData as Entity
        val entityB = bodyB.userData as Entity

        // Find the specific entities that interest us
        val bulletEntity = findEntity(PlayerComponent, entityA, entityB)
        val platformEntity = findEntity(PlatformComponent, entityA, entityB)

        // A bullet and a platform collided
        if (bulletEntity != null && platformEntity != null)
            gameEventSignal.dispatch(GameEvent.BULLET_PLATFORM_COLLISION)
    }

    // --------------- Not used ContactListener functions ---------------
    override fun endContact(contact: Contact?) {}

    override fun preSolve(contact: Contact?, oldManifold: Manifold?) {}

    override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {}
}