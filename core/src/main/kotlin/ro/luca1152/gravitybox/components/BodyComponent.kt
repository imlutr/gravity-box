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
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.bodies
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Contains a Box2D body. */
class BodyComponent(private val world: World = Injekt.get()) : Component, Poolable {
    var body: Body = world.createBody(BodyDef())

    fun set(body: Body, userData: Entity) {
        this.body = body
        body.userData = userData
    }

    override fun reset() {
        if (world.bodies.contains(body, false))
            world.destroyBody(body)
    }

    companion object : ComponentResolver<BodyComponent>(BodyComponent::class.java)
}

val Entity.body: BodyComponent
    get() = BodyComponent[this]