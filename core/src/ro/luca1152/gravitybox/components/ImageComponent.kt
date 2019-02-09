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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.actors.minus
import ktx.actors.plus
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Contains the rendering data for an entity, such as the position or texture.
 * Is an Image (from Scene2D) in order to be able to use Actions.
 */
class ImageComponent(private val stage: GameStage = Injekt.get()) : Component, Poolable {
    // Initialized with an empty image to avoid nullable type
    // Called img to avoid confusion between entity.image and entity.image.img
    var img: Image = Image()

    // Getters and setters for easier access (entity.image.[x] instead of entity.image.img.[x])
    // Entity.image returns ImageComponent and not Image so set(texture, x, y) can be used
    var width: Float
        get() = img.width
        set(value) {
            img.width = value
        }
    var height: Float
        get() = img.height
        set(value) {
            img.height = value
        }
    var x: Float
        get() = img.x
        set(value) {
            img.x = value
        }
    var y: Float
        get() = img.y
        set(value) {
            img.y = value
        }
    var color: Color
        get() = img.color
        set(value) {
            img.color = value
        }

    /** Initializes the component. */
    fun set(texture: Texture, x: Float, y: Float) {
        img.run {
            drawable = TextureRegionDrawable(TextureRegion(texture))
            setPosition(x, y)
            setSize(texture.width.pixelsToMeters, texture.height.pixelsToMeters)
            setOrigin(width / 2f, height / 2f)
        }
        stage + img
    }

    /** Initializes the component. */
    fun set(texture: Texture, position: Vector2) = set(texture, position.x, position.y)

    /** Resets the component for reuse. */
    override fun reset() {
        stage - img
        img.run {
            color = Color.WHITE
            rotation = 0f
            scaleX = 1f; img.scaleY = 1f
            actions.forEach {
                removeAction(it)
            }
        }
    }

    companion object : ComponentResolver<ImageComponent>(ImageComponent::class.java)
}

val Entity.image: ImageComponent
    get() = ImageComponent[this]