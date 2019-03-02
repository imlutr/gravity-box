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
import ro.luca1152.gravitybox.components.game.ImageComponent
import ro.luca1152.gravitybox.components.game.image
import ro.luca1152.gravitybox.utils.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Used to expand the touchable bounds of an image, so even if you touch outside the image, a click
 * will still register. If an entity doesn't have this component, its bounds will be the default size.
 */
class TouchableBoundsComponent(private val gameStage: GameStage = Injekt.get()) : Component, Poolable {
    private var extraWidth = 0f
    private var extraHeight = 0f
    var boundsImage = Image()

    fun set(linkedEntity: Entity, extraWidth: Float, extraHeight: Float) {
        require(linkedEntity.tryGet(ImageComponent) != null)
        { "The entity must have an ImageComponent so the [boundsImage] can be positioned." }

        setExtraSize(extraWidth, extraHeight)
        setSize(linkedEntity.image.width, linkedEntity.image.height)
        setPosition(linkedEntity.image.centerX, linkedEntity.image.centerY)
        boundsImage.run {
            color = Color.CLEAR
            userObject = linkedEntity
        }
        gameStage.addActor(boundsImage)
    }

    fun setPosition(centerX: Float, centerY: Float) {
        require(boundsImage.width != 0f && boundsImage.height != 0f)
        { "Setting the position based on the center coordinates requires the size to be set." }

        boundsImage.run {
            x = centerX - width / 2f
            y = centerY - height / 2f
        }
    }

    private fun setExtraSize(extraWidth: Float, extraHeight: Float) {
        require(extraWidth >= 0f && extraHeight >= 0f)

        this.extraWidth = extraWidth
        this.extraHeight = extraHeight
    }

    /** Sets both the size and the origin. */
    fun setSize(imageWidth: Float, imageHeight: Float) {
        require(imageWidth != 0f && imageHeight != 0f)

        boundsImage.run {
            setSize(imageWidth + extraWidth, imageHeight + extraHeight)
            setOrigin(width / 2f, height / 2f)
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

    companion object : ComponentResolver<TouchableBoundsComponent>(TouchableBoundsComponent::class.java)
}

val Entity.touchableBounds: TouchableBoundsComponent
    get() = TouchableBoundsComponent[this]