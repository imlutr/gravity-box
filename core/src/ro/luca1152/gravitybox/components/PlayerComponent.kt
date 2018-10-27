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
import com.badlogic.gdx.utils.Pool.Poolable

/**
 * Indicates that the entity is a player.
 */
class PlayerComponent : Component, Poolable {
    companion object : ComponentResolver<PlayerComponent>(PlayerComponent::class.java)

    /**
     * Reset the player to its initial state (initial position & no velocity).
     * Used when restarting the level.
     */
    fun reset(body: Body) {
        body.setTransform(0f, 0f, 0f) // Reset the position
        body.applyForceToCenter(0f, 0f, true) // Wake the body so it doesn't float
        body.setLinearVelocity(0f, 0f)
        body.angularVelocity = 0f
    }

    override fun reset() {}
}

val Entity.player: PlayerComponent
    get() = PlayerComponent[this]