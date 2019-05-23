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

package ro.luca1152.gravitybox.systems.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Pools
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.ExplosionImageEntity
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.tryGet


/** Handles what happens when a bullet collides with a map object. */
class BulletCollisionSystem(private val context: Context) :
    IteratingSystem(Family.all(BulletComponent::class.java).get()) {
    private val world: World = context.inject()
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        playerEntity = engine.getSingleton<PlayerComponent>()
        finishEntity = engine.getSingleton<FinishComponent>()
    }

    override fun processEntity(bullet: Entity, deltaTime: Float) {
        if (bullet.bullet.collidedWithPlatform) {
            val bulletPosition = bullet.body.body!!.worldCenter
            ExplosionImageEntity.createEntity(context, bulletPosition.x, bulletPosition.y)
            applyBlastImpulse(bullet.body.body!!)
            engine.removeEntity(bullet)
        }
    }

    private fun applyBlastImpulse(bullet: Body) {
        val playerBody = playerEntity.body.body
        val closestBody = getClosestBodyToExplosion(bullet.worldCenter, playerBody!!.worldCenter)
        if (noObstacleFoundBetween(closestBody)) {
            playerBody.applyBlastImpulse(bullet.worldCenter, playerBody.worldCenter, 150f)
            if (playerEntity.tryGet(PassengerComponent) != null) {
                val driverVelocity = playerEntity.passenger.driver!!.body.body!!.linearVelocity
                playerBody.setLinearVelocity(
                    playerBody.linearVelocity.x - driverVelocity.x,
                    playerBody.linearVelocity.y - driverVelocity.y
                )
            }
        }
    }

    private fun getClosestBodyToExplosion(explosionCenter: Vector2, playerCenter: Vector2): Body? {
        var closestBody: Body? = null
        world.rayCast({ fixture, _, _, fraction ->
            closestBody = fixture.body
            fraction
        }, explosionCenter, playerCenter)
        return closestBody
    }

    private fun noObstacleFoundBetween(closestBody: Body?) =
        closestBody == playerEntity.body.body || closestBody == finishEntity.body.body || closestBody == null

    private fun Body.applyBlastImpulse(blastCenter: Vector2, applyPoint: Vector2, blastPower: Float) {
        // Apply only on dynamic bodies so the impulse has an effect
        if (this.type != BodyDef.BodyType.DynamicBody)
            return

        // Calculate the distance
        val blastDir = Pools.obtain(Vector2::class.java).set(applyPoint).sub(blastCenter)
        val distance = blastDir.len()

        // Calculate the inverse distance
        if (distance == 0f) return
        val invDistance = 1f / distance

        // Calculate the impulse's magnitude
        val impulseMag = Math.min(blastPower * invDistance * invDistance, 21f)

        // Apply the force
        this.applyLinearImpulse(
            blastDir.nor().scl(impulseMag),
            playerEntity.body.body!!.worldCenter,
            true
        )
        Pools.free(blastDir)
    }
}

