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

package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Pool.Poolable
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Contains a Box2D body.
 */
class PhysicsComponent(world: World = Injekt.get()) : Component, Poolable {
    // Initialized with an empty body to avoid nullable type
    var body: Body = world.createBody(BodyDef())

    fun set(body: Body) {
        this.body = body
    }

    override fun reset() {
        body.setTransform(0f, 0f, 0f) // Reset the position
        body.applyForceToCenter(0f, 0f, true) // Wake the body so it doesn't float
        body.setLinearVelocity(0f, 0f)
        body.angularVelocity = 0f
    }

    companion object : ComponentResolver<PhysicsComponent>(PhysicsComponent::class.java)
}

val Entity.physics: PhysicsComponent
    get() = PhysicsComponent[this]