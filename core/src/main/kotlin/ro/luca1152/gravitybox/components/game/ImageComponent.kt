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
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Pool.Poolable
import com.badlogic.gdx.utils.Pools
import ktx.actors.minus
import ktx.actors.plus
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.getRectangleCenter
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Contains an [Image]. */
class ImageComponent(private val stage: GameStage = Injekt.get()) : Component, Poolable {
    var img: Image = Image()

    var width: Float
        get() = img.width
        set(value) {
            img.width = value
            updateOrigin()
        }
    var height: Float
        get() = img.height
        set(value) {
            img.height = value
            updateOrigin()
        }

    var centerX: Float
        get() {
            if (width == 0f)
                error { "The width can't be 0." }
            return img.x + width / 2f
        }
        set(value) {
            if (width == 0f)
                error { "The width can't be 0." }
            img.x = value - width / 2f
        }
    var leftX: Float
        get() = img.x
        set(value) {
            img.x = value
        }
    var rightX: Float
        get() = img.x + width
        set(value) {
            img.x = value - width
        }
    var centerY: Float
        get() {
            if (height == 0f)
                error { "The height can't be 0." }
            return img.y + height / 2f
        }
        set(value) {
            if (height == 0f)
                error { "The height can't be 0." }
            img.y = value - height / 2f
        }
    var bottomY: Float
        get() = img.y
        set(value) {
            img.y = value
        }
    var topY: Float
        get() = img.y + height
        set(value) {
            img.y = value - height
        }

    var color: Color
        get() = img.color
        set(value) {
            img.color = value
        }

    fun set(
        textureRegion: TextureRegion,
        x: Float, y: Float,
        width: Float = 0f, height: Float = 0f,
        rotationInDeg: Float = 0f
    ) =
        set(TextureRegionDrawable(textureRegion), x, y, width, height, rotationInDeg)

    fun set(texture: Texture, x: Float, y: Float, width: Float = 0f, height: Float = 0f, rotationInDeg: Float = 0f) =
        set(TextureRegion(texture), x, y, width, height, rotationInDeg)

    fun set(texture: Texture, position: Vector2) = set(texture, position.x, position.y)

    fun set(
        ninePatch: NinePatch,
        x: Float, y: Float,
        width: Float = 0f, height: Float = 0f,
        rotationInDeg: Float = 0f
    ) {
        ninePatch.scale(1 / PPM, 1 / PPM)
        img.run {
            this.drawable = NinePatchDrawable(ninePatch)
            when (width == 0f && height == 0f) {
                true -> setSize(drawable.minWidth, drawable.minHeight)
                false -> setSize(width, height)
            }
            setOrigin(this.width / 2f, this.height / 2f)
            rotation = rotationInDeg
        }
        this.setPosition(x, y)
        stage + img
    }

    fun set(
        drawable: Drawable,
        x: Float, y: Float,
        width: Float = 0f, height: Float = 0f,
        rotationInDeg: Float = 0f
    ) {
        img.run {
            this.drawable = drawable
            when (width == 0f && height == 0f) {
                true -> setSize(drawable.minWidth.pixelsToMeters, drawable.minHeight.pixelsToMeters)
                false -> setSize(width, height)
            }
            setOrigin(this.width / 2f, this.height / 2f)
            rotation = rotationInDeg
        }
        this.setPosition(x, y)
        stage + img
    }

    fun setPosition(x: Float, y: Float) {
        this.centerX = x
        this.centerY = y
    }

    private fun updateOrigin() {
        img.run {
            originX = width / 2f
            originY = height / 2f
        }
    }

    /**
     * Creates a [Box2D] body based on the image's size and rotation. It should be used only
     * if the size of the intended body is the same as the entity's image.
     */
    fun imageToBox2DBody(
        bodyType: BodyDef.BodyType,
        categoryBits: Short = EntityCategory.OBSTACLE.bits, maskBits: Short = EntityCategory.OBSTACLE.bits,
        density: Float = 1f, friction: Float = 0.2f,
        trimSize: Float = 0f,
        world: World = Injekt.get()
    ): Body {
        val bodyDef = Pools.obtain(BodyDef::class.java).apply {
            type = bodyType
            position.set(0f, 0f)
            fixedRotation = false
            bullet = false
        }
        val polygonShape = PolygonShape().apply {
            setAsBox(width / 2f - trimSize, height / 2f - trimSize)
        }
        val fixtureDef = FixtureDef().apply {
            shape = polygonShape
            filter.categoryBits = categoryBits
            filter.maskBits = maskBits
            this.density = density
            this.friction = friction
        }
        return world.createBody(bodyDef).apply {
            createFixture(fixtureDef)
            polygonShape.dispose()
            setTransform(centerX, centerY, img.rotation * MathUtils.degreesToRadians)
            Pools.free(bodyDef)
        }
    }

    fun updateFromPolygon(polygon: Polygon) {
        val vertices = polygon.vertices
        val xCoords = vertices.filterIndexed { index, _ -> index % 2 == 0 }.sortedBy { it }
        val yCoords = vertices.filterIndexed { index, _ -> index % 2 == 1 }.sortedBy { it }

        val width = xCoords.last() - xCoords.first()
        val height = yCoords.last() - yCoords.first()
        val center = polygon.getRectangleCenter()

        this.width = width
        this.height = height
        this.centerX = center.x
        this.centerY = center.y

    }

    override fun reset() {
        stage - img
        img.run {
            color = Color.WHITE
            rotation = 0f
            scaleX = 1f; scaleY = 1f
            isVisible = true
            touchable = Touchable.enabled
            clear()
        }
    }

    companion object : ComponentResolver<ImageComponent>(ImageComponent::class.java)
}

val Entity.image: ImageComponent
    get() = ImageComponent[this]