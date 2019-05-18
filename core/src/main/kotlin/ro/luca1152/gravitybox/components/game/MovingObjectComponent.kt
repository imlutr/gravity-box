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
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ktx.math.minus
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.editor.MockMapObjectComponent
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Indicates that the map object is moving back an forth to a given position. */
class MovingObjectComponent : Component, Poolable {
    companion object : ComponentResolver<MovingObjectComponent>(MovingObjectComponent::class.java) {
        const val SPEED = 3.5f
        const val DELAY_BEFORE_SWITCHING_DIRECTION = 5 / 60f // 5 frames
    }

    val startPoint = Vector2()
    val endPoint = Vector2()
    val startToFinishDirection = Vector2()
    var startToFinishDistance = 0f
    var speed = SPEED
    var delayBeforeSwitching = 0f
    var justSwitchedDirection = true

    /** If false, it means that the platform is moving back towards the starting point. */
    var isMovingTowardsEndPoint = true

    fun set(platformEntity: Entity, targetX: Float, targetY: Float, speed: Float) {
        startPoint.set(platformEntity.scene2D.centerX, platformEntity.scene2D.centerY)
        endPoint.set(targetX, targetY)
        this.speed = speed
        update()
    }

    fun moved(platformEntity: Entity? = null, mockPlatformEntity: Entity? = null) {
        require(platformEntity?.tryGet(PlatformComponent) != null || platformEntity?.tryGet(DestroyablePlatformComponent) != null)
        { "The provided platformEntity is not a platform." }

        require(mockPlatformEntity?.tryGet(MockMapObjectComponent) != null)
        { "The provided mockPlatformEntity is not a mock platform." }

        platformEntity?.let {
            startPoint.set(platformEntity.scene2D.centerX, platformEntity.scene2D.centerY)
        }
        mockPlatformEntity?.let {
            endPoint.set(mockPlatformEntity.scene2D.centerX, mockPlatformEntity.scene2D.centerY)
        }
        update()
    }

    fun update() {
        updateStartToFinishDistance()
        updateDirection()
    }

    private fun updateStartToFinishDistance() {
        startToFinishDistance = startPoint.dst(endPoint)
    }

    private fun updateDirection() {
        startToFinishDirection.set(endPoint - startPoint)
        startToFinishDirection.nor()
    }

    override fun reset() {
        startPoint.set(0f, 0f)
        endPoint.set(0f, 0f)
        startToFinishDirection.set(0f, 0f)
        startToFinishDistance = 0f
        delayBeforeSwitching = 0f
        speed = SPEED
        justSwitchedDirection = true
    }
}

val Entity.movingObject: MovingObjectComponent
    get() = MovingObjectComponent[this]

fun Entity.movingObject(
    context: Context,
    targetX: Float, targetY: Float,
    speed: Float
) = add(createComponent<MovingObjectComponent>(context).apply {
    set(this@movingObject, targetX, targetY, speed)
})!!