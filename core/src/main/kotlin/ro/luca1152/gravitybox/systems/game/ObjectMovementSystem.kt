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
import com.badlogic.gdx.utils.Pools
import ktx.math.times
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.tryGet


class ObjectMovementSystem : IteratingSystem(Family.all(MovingObjectComponent::class.java).get()) {
    private lateinit var playerEntity: Entity

    override fun addedToEngine(engine: Engine) {
        super.addedToEngine(engine)
        playerEntity = engine.getSingleton<PlayerComponent>()
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.tryGet(LinkedEntityComponent) != null && entity.linkedEntity.get("mockPlatform").editorObject.isDeleted) {
            return
        }
        if (entity.tryGet(DestroyablePlatformComponent) != null && entity.destroyablePlatform.isRemoved) {
            return
        }
        entity.movingObject.delayBeforeSwitching -= deltaTime
        moveObject(entity)
    }

    private fun moveObject(entity: Entity) {
        entity.run {
            val moveBy = getMoveBy(entity)
            updatePosition(entity, moveBy)
            updateDirection(entity)
            Pools.free(moveBy)
        }
    }

    private fun getMoveBy(entity: Entity): Vector2 {
        entity.run {
            val moveBy = Pools.obtain(Vector2::class.java).set(movingObject.startToFinishDirection * movingObject.speed)
            if (!movingObject.isMovingTowardsEndPoint) {
                moveBy.set(moveBy * -1)
            }
            return moveBy
        }
    }

    private fun updatePosition(entity: Entity, moveBy: Vector2) {
        entity.body.body!!.setLinearVelocity(moveBy.x, moveBy.y)
        if (entity.movingObject.justSwitchedDirection) {
            entity.movingObject.justSwitchedDirection = false
            if (playerEntity.tryGet(PassengerComponent) != null && playerEntity.passenger.driver == entity) {
                playerEntity.body.body!!.linearVelocity = moveBy
            }
        }
    }

    private fun updateDirection(entity: Entity) {
        if (entity.movingObject.delayBeforeSwitching <= 0f) {
            entity.run {
                val objectPosition = Pools.obtain(Vector2::class.java).set(scene2D.centerX, scene2D.centerY)
                if (
                    movingObject.isMovingTowardsEndPoint &&
                    (objectPosition.dst(movingObject.startPoint).approxEqualTo(movingObject.startToFinishDistance) ||
                            objectPosition.dst(movingObject.startPoint) >= movingObject.startToFinishDistance)
                ) {
                    objectPosition.set(movingObject.endPoint)
                    movingObject.run {
                        justSwitchedDirection = true
                        isMovingTowardsEndPoint = false
                        delayBeforeSwitching = MovingObjectComponent.DELAY_BEFORE_SWITCHING_DIRECTION
                    }
                } else if (
                    !movingObject.isMovingTowardsEndPoint &&
                    (objectPosition.dst(movingObject.endPoint).approxEqualTo(movingObject.startToFinishDistance) ||
                            objectPosition.dst(movingObject.endPoint) >= movingObject.startToFinishDistance)
                ) {
                    objectPosition.set(movingObject.startPoint)
                    movingObject.run {
                        justSwitchedDirection = true
                        isMovingTowardsEndPoint = true
                        delayBeforeSwitching = MovingObjectComponent.DELAY_BEFORE_SWITCHING_DIRECTION
                    }
                }
                Pools.free(objectPosition)
            }
        }
    }

    private fun Float.approxEqualTo(f: Float) = Math.abs(this - f) <= 0.001f
}