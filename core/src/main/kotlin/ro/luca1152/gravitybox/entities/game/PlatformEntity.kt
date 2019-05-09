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

@file:Suppress("MemberVisibilityCanBePrivate")

package ro.luca1152.gravitybox.entities.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.NinePatch
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity

object PlatformEntity {
    const val PATCH_LEFT = 9
    const val PATCH_RIGHT = 9
    const val PATCH_TOP = 5
    const val PATCH_BOTTOM = 5
    const val ROTATION = 0f
    const val HEIGHT = .25f
    val CATEGORY_BITS = EntityCategory.PLATFORM.bits
    val MASK_BITS = EntityCategory.OBSTACLE.bits

    fun createEntity(
        context: Context,
        x: Float, y: Float,
        width: Float,
        rotation: Float = ROTATION,
        isDestroyable: Boolean = false,
        isRotating: Boolean = false,
        targetX: Float = Float.POSITIVE_INFINITY,
        targetY: Float = Float.POSITIVE_INFINITY,
        speed: Float = MovingObjectComponent.SPEED
    ) = newEntity(context).apply {
        val manager: AssetManager = context.inject()
        mapObject(context)
        if (isDestroyable) {
            destroyablePlatform(context)
            scene2D(
                context,
                NinePatch(
                    manager.get(Assets.tileset).findRegion("platform-0"),
                    PATCH_LEFT, PATCH_RIGHT,
                    PATCH_TOP, PATCH_BOTTOM
                ), x, y, width, HEIGHT, rotation
            )
        } else {
            platform(context)
            scene2D(
                context,
                NinePatch(
                    manager.get(Assets.tileset).findRegion("platform-0"),
                    PATCH_LEFT, PATCH_RIGHT,
                    PATCH_TOP, PATCH_BOTTOM
                ), x, y, width, HEIGHT, rotation
            )
        }
        if (isRotating) {
            rotatingObject(context)
        }
        if (targetX != Float.POSITIVE_INFINITY && targetY != Float.POSITIVE_INFINITY) {
            movingObject(context, targetX, targetY, speed)
        }
        polygon(context, scene2D)
        editorObject(context)
        snap(context)
        body(context)
        color(context, ColorType.DARK)
        overlay(
            context,
            showMovementButtons = true, showRotationButton = true, showDeletionButton = true,
            showResizingButtons = true, showSettingsButton = true
        )
        extendedTouch(context, this, 0f, 1f - HEIGHT)
        json(context, this)
        addToEngine(context)
    }
}