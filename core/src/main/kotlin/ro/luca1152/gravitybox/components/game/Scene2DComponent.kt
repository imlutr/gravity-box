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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.engine
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class Scene2DComponent(private val gameStage: GameStage = Injekt.get()) : Component, Poolable {
    private lateinit var mockImage: ImageComponent
    val group = Group()

    var width: Float
        get() = group.width
        set(value) {
            group.width = value
        }
    var height: Float
        get() = group.height
        set(value) {
            group.height = value
        }
    var centerX: Float
        get() {
            require(width != 0f) { "The width can't be 0." }
            return group.x + width / 2f
        }
        set(value) {
            require(width != 0f) { "The width can't be 0." }
            group.x = value - width / 2f
        }
    var leftX: Float
        get() = group.x
        set(value) {
            group.x = value
        }
    var rightX: Float
        get() {
            require(width != 0f) { "The width can't be 0." }
            return group.x + width
        }
        set(value) {
            require(width != 0f) { "The width can't be 0." }
            group.x = value - width
        }
    var centerY: Float
        get() {
            require(height != 0f) { "The height can't be 0." }
            return group.y + height / 2f
        }
        set(value) {
            require(height != 0f) { "The height can't be 0." }
            group.y = value - height / 2f
        }
    var bottomY: Float
        get() = group.y
        set(value) {
            group.y = value
        }
    var topY: Float
        get() {
            require(height != 0f) { "The height can't be 0." }
            return group.y + height
        }
        set(value) {
            require(height != 0f) { "The height can't be 0." }
            group.y = value - height
        }
    var color: Color
        get() = group.color
        set(value) {
            group.color = value
            group.children.forEach {
                it.color = value
            }
        }
    var rotation: Float
        get() = group.rotation
        set(value) {
            group.rotation = value
        }
    var userData: Any?
        get() = group.userObject
        set(value) {
            group.userObject = value
        }
    var isVisible: Boolean
        get() = group.isVisible
        set(value) {
            group.isVisible = value
        }

    fun addImage(
        textureRegion: TextureRegion,
        centerX: Float = 0f, centerY: Float = 0f,
        width: Float = 0f, height: Float = 0f,
        rotation: Float = 0f
    ) = addImage(TextureRegionDrawable(textureRegion), centerX, centerY, width, height, rotation)

    fun addNinePatch(
        ninePatch: NinePatch,
        centerX: Float = 0f, centerY: Float = 0f,
        width: Float = 0f, height: Float = 0f,
        rotation: Float = 0f
    ): Image {
        ninePatch.scale(1 / PPM, 1 / PPM)
        return addImage(NinePatchDrawable(ninePatch), centerX, centerY, width, height, rotation)
    }

    fun addImage(
        drawable: Drawable,
        centerX: Float = 0f, centerY: Float = 0f,
        width: Float = 0f, height: Float = 0f,
        rotation: Float = 0f
    ): Image {
        val image = Image(drawable).apply {
            if (width != 0f && height != 0f) {
                setSize(width, height)
            }
            setPosition(centerX - width / 2f, centerY - height / 2f)
            setOrigin(width / 2f, height / 2f)
            rotateBy(rotation)
        }
        group.run {
            addActor(image)
            setSize(this.width + image.width, this.height + image.height)
            setOrigin(this.width / 2f, this.height / 2f)
        }
        return image
    }

    fun set(mockImage: ImageComponent) {
        group.debugAll()
        this.mockImage = mockImage
        gameStage.addActor(group)
    }

    override fun reset() {
        group.run {
            clear()
            remove()
        }
    }

    companion object : ComponentResolver<Scene2DComponent>(Scene2DComponent::class.java)
}

val Entity.scene2D: Scene2DComponent
    get() = Scene2DComponent[this]

fun Entity.scene2D(mockImage: ImageComponent) = add(
    engine.createComponent(Scene2DComponent::class.java).apply {
        set(mockImage)
    })!!