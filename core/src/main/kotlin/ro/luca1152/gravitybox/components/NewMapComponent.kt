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
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Suppress("PrivatePropertyName")
class NewMapComponent : Component, Poolable {
    companion object : ComponentResolver<NewMapComponent>(NewMapComponent::class.java) {
        const val GRAVITY = -25f
    }

    val world: World = Injekt.get()

    /**
     * The level number of the currently stored map. It may differ from the level intended
     * to be played stored in [LevelComponent].
     */
    var levelNumber = 0

    override fun reset() {
        destroyAllBodies()
        levelNumber = 0
    }

    private fun destroyAllBodies() {
        val bodiesToRemove = Array<Body>()
        world.getBodies(bodiesToRemove)
        bodiesToRemove.forEach {
            world.destroyBody(it)
        }
    }
}

val Entity.newMap: NewMapComponent
    get() = NewMapComponent[this]