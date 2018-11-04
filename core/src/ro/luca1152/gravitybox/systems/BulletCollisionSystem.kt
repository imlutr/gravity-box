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

package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.removeAndResetEntity
import ro.luca1152.gravitybox.components.utils.tryGet
import ro.luca1152.gravitybox.entities.EntityFactory
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Handles what happens when a bullet collides with a platform.
 */
class BulletCollisionSystem :
    IteratingSystem(Family.all(BulletComponent::class.java, ImageComponent::class.java).get()) {
    override fun processEntity(bullet: Entity, deltaTime: Float) {
        if (bullet.bullet.collidedWithWall) {
            // Modify the position so the bullet will be inside the platform it collided with.
            // If I wouldn't do this, there would be no explosion force if the player shot down, for example.
            val bPos = bullet.physics.body.worldCenter
            val pPos = bullet.bullet.collidedWith.mapObject.worldCenter
            bPos.set(
                if (bPos.x.approxEqual(pPos.x)) pPos.x else bPos.x,
                if (bPos.y.approxEqual(pPos.y)) pPos.y else bPos.y
            )

            // Create the actual explosion by applying a force
            explodeAt(bPos)

            // Create the explosion image
            EntityFactory.createExplosion(bullet.physics.body.worldCenter)

            engine.removeAndResetEntity(bullet)
        }
    }

    private fun Float.approxEqual(x: Float, epsilon: Float = 0.4f): Boolean {
        return Math.abs(this - x) <= epsilon
    }

    /**
     * Creates a raycast explosion.
     * Code from https://mentalgrain.com/box2d/explosions-in-box2d/
     */
    private fun explodeAt(
        center: Vector2,
        world: World = Injekt.get()
    ) {
        // Constants
        val numRays = 15
        val blastPower = 200f
        val blastRadius = 10

        // Vector2's
        val rayDir = Vector2()
        val rayEnd = Vector2()

        for (i in 0 until numRays) {
            val angle = i.toFloat() / numRays * 360 * MathUtils.degRad
            rayDir.set(MathUtils.sin(angle), MathUtils.cos(angle))
            rayEnd.set(
                center.x + blastRadius * rayDir.x,
                center.y + blastRadius * rayDir.y
            )
            world.rayCast({ fixture, point, _, _ ->
                if ((fixture.body.userData as Entity).tryGet(PlayerComponent) != null)
                    fixture.body.applyBlastImpulse(
                        center,
                        point,
                        blastPower / numRays.toFloat()
                    )
                0f
            }, center, rayEnd)
        }
    }

    private fun Body.applyBlastImpulse(blastCenter: Vector2, applyPoint: Vector2, blastPower: Float) {
        if (this.type != BodyDef.BodyType.DynamicBody)
            return

        val blastDir = applyPoint.cpy().sub(blastCenter)
        val distance = blastDir.len()
        if (distance == 0f) return

        val invDistance = 1f / distance
        val impulseMag = Math.min(blastPower * invDistance * invDistance, 7.5f)

        this.applyLinearImpulse(blastDir.nor().scl(impulseMag), applyPoint, true)
    }
}

