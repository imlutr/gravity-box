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

package ro.luca1152.gravitybox.components.editor

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.game.Scene2DComponent
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Expands the isTouchable area of an entity. */
class ExtendedTouchComponent : Component, Poolable {
    private var extraWidth = 0f
    private var extraHeight = 0f
    var boundsImage = Image()

    fun set(context: Context, linkedEntity: Entity, extraWidth: Float, extraHeight: Float) {
        require(linkedEntity.tryGet(Scene2DComponent) != null)
        { "The entity must have an Scene2DComponent so the extended touch image can be positioned." }

        val gameStage = context.inject<GameStage>()

        this.extraWidth = extraWidth
        this.extraHeight = extraHeight
        boundsImage.run {
            color = Color.CLEAR
            userObject = linkedEntity
        }
        gameStage.addActor(boundsImage)
    }

    fun updateFromScene2D(scene2D: Scene2DComponent) {
        boundsImage.run {
            setSize(scene2D.width + extraWidth, scene2D.height + extraHeight)
            setPosition(scene2D.leftX - extraWidth / 2f, scene2D.bottomY - extraHeight / 2f)
            setOrigin(width / 2f, height / 2f)
            rotation = scene2D.rotation
        }
    }

    override fun reset() {
        boundsImage.run {
            remove()
            rotation = 0f
            actions.forEach {
                removeAction(it)
            }
            userObject = null
        }
    }

    companion object : ComponentResolver<ExtendedTouchComponent>(ExtendedTouchComponent::class.java)
}

val Entity.extendedTouch: ExtendedTouchComponent
    get() = ExtendedTouchComponent[this]

fun Entity.extendedTouch(
    context: Context,
    linkedEntity: Entity,
    extraWidth: Float, extraHeight: Float
) =
    add(createComponent<ExtendedTouchComponent>(context).apply {
        set(context, linkedEntity, extraWidth, extraHeight)
    })!!