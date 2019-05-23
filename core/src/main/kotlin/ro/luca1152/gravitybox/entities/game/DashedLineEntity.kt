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

package ro.luca1152.gravitybox.entities.game

import com.badlogic.ashley.core.Entity
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.MockMapObjectComponent
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity
import ro.luca1152.gravitybox.utils.kotlin.tryGet

object DashedLineEntity {
    fun createEntity(
        context: Context,
        startX: Float, startY: Float,
        endX: Float, endY: Float
    ) = newEntity(context).apply {
        dashedLine(
            context,
            startX, startY,
            endX, endY
        )
        addToEngine(context)
    }

    fun createEntity(
        context: Context,
        platformEntity: Entity, mockPlatformEntity: Entity
    ) = createEntity(
        context,
        platformEntity.scene2D.centerX, platformEntity.scene2D.centerY,
        mockPlatformEntity.scene2D.centerX, mockPlatformEntity.scene2D.centerY
    ).apply {
        linkedEntity(context)
        linkedEntity.apply {
            require(
                platformEntity.tryGet(PlatformComponent) != null || platformEntity.tryGet(DestroyablePlatformComponent) != null
            ) { "The given platformEntity is not a platform." }

            require(mockPlatformEntity.tryGet(MockMapObjectComponent) != null)
            { "The given mockPlatformEntity is not a mock platform." }

            add("platform", platformEntity)
            add("mockPlatform", mockPlatformEntity)
        }
    }
}