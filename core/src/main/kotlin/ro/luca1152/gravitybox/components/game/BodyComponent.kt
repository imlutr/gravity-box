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

package ro.luca1152.gravitybox.components.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.bodies
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Contains a Box2D body. */
class BodyComponent(private val world: World = Injekt.get()) : Component, Poolable {
    // The properties of the body at the moment of its creation. Changing these values does NOT affect the body.
    var bodyType = BodyDef.BodyType.StaticBody
    var density = 1f
    var friction = .2f
    var categoryBits: Short = 0
    var maskBits: Short = 0

    var initialX = Float.POSITIVE_INFINITY
    var initialY = Float.POSITIVE_INFINITY
    var initialRotationRad = 0f

    lateinit var body: Body

    fun set(
        body: Body, userData: Entity,
        categoryBits: Short = EntityCategory.NONE.bits, maskBits: Short = EntityCategory.NONE.bits,
        density: Float = 1f, friction: Float = .2f
    ) {
        this.body = body
        body.userData = userData

        // Update properties
        bodyType = body.type
        this.density = density
        this.friction = friction
        this.categoryBits = categoryBits
        this.maskBits = maskBits

        // Update initial values
        initialX = body.position.x
        initialY = body.position.y
        initialRotationRad = body.angle
    }

    fun resetToInitialState() {
        if (initialX != Float.POSITIVE_INFINITY && initialY != Float.POSITIVE_INFINITY && bodyType != BodyDef.BodyType.StaticBody) {
            body.run {
                setTransform(initialX, initialY, initialRotationRad)
                applyForceToCenter(0f, 0f, true) // Wake up the body so it doesn't float
                setLinearVelocity(0f, 0f)
                angularVelocity = 0f
            }
        }
    }

    override fun reset() {
        destroyBody()
        resetInitialState()
    }

    fun resetInitialState() {
        initialX = Float.POSITIVE_INFINITY
        initialY = Float.POSITIVE_INFINITY
        initialRotationRad = 0f
    }

    fun destroyBody() {
        if (::body.isInitialized) {
            if (world.bodies.contains(body, false))
                world.destroyBody(body)
        }
    }

    companion object : ComponentResolver<BodyComponent>(BodyComponent::class.java)
}

val Entity.body: BodyComponent
    get() = BodyComponent[this]