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
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.kotlin.getRectangleCenter

@Suppress("MemberVisibilityCanBePrivate")
class Scene2DComponent : Component, Poolable {
    val group = object : Group() {
        // The default implementation of Group's hit() is to return the children
        // However, I want to return the group only, treating the object as a whole
        override fun hit(x: Float, y: Float, touchable: Boolean): Actor? {
            return if (super.hit(x, y, touchable) != null) this else null
        }
    }
    var width: Float
        get() = group.width
        set(value) {
            group.width = value
            centerOrigin()
        }
    var height: Float
        get() = group.height
        set(value) {
            group.height = value
            centerOrigin()
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
    var originX: Float
        get() = group.originX
        set(value) {
            group.originX = value
        }
    var originY: Float
        get() = group.originY
        set(value) {
            group.originY = value
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
    var userObject: Any?
        get() = group.userObject
        set(value) {
            group.userObject = value
        }
    var isVisible: Boolean
        get() = group.isVisible
        set(value) {
            group.isVisible = value
        }
    var isTouchable: Boolean
        get() = group.isTouchable
        set(value) {
            group.touchable = if (value) Touchable.enabled else Touchable.disabled
        }

    private fun centerOrigin() {
        originX = width / 2f
        originY = height / 2f
    }

    fun addNinePatch(
        context: Context,
        ninePatch: NinePatch,
        centerX: Float = 0f, centerY: Float = 0f,
        width: Float = 0f, height: Float = 0f,
        rotation: Float = 0f,
        appendWidth: Boolean = true, appendHeight: Boolean = true
    ): Image {
        ninePatch.scale(1 / PPM, 1 / PPM)
        return addImage(
            context,
            NinePatchDrawable(ninePatch),
            centerX, centerY,
            width, height,
            rotation,
            appendWidth, appendHeight
        )
    }

    fun addImage(
        context: Context,
        textureRegion: TextureRegion,
        centerX: Float = 0f, centerY: Float = 0f,
        width: Float = 0f, height: Float = 0f,
        rotation: Float = 0f,
        appendWidth: Boolean = true, appendHeight: Boolean = true
    ): Image {
        val textureRegionDrawable = TextureRegionDrawable(textureRegion).apply {
            minWidth /= PPM
            minHeight /= PPM
        }
        return addImage(
            context,
            textureRegionDrawable,
            centerX,
            centerY,
            width,
            height,
            rotation,
            appendWidth,
            appendHeight
        )
    }

    private fun addImage(
        context: Context,
        drawable: Drawable,
        centerX: Float = 0f, centerY: Float = 0f,
        width: Float = 0f, height: Float = 0f,
        rotation: Float = 0f,
        appendWidth: Boolean = true, appendHeight: Boolean = true
    ): Image {
        val gameStage: GameStage = context.inject()
        val image = Image(drawable).apply {
            if (width != 0f && height != 0f) {
                setSize(width, height)
            }
        }
        group.run {
            addActor(image)
            setSize(
                group.width + (if (appendWidth) image.width else 0f),
                group.height + (if (appendHeight) image.height else 0f)
            )
            setOrigin(this@Scene2DComponent.width / 2f, this@Scene2DComponent.height / 2f)
            this.rotation = rotation
            gameStage.addActor(this)
        }
        this.centerX = centerX
        this.centerY = centerY
        return image
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

    fun toBody(
        context: Context,
        bodyType: BodyDef.BodyType,
        categoryBits: Short,
        maskBits: Short,
        density: Float = 1f,
        friction: Float = 0.2f,
        trimSize: Float = 0f
    ): Body {
        val world: World = context.inject()
        val bodyDef = BodyDef().apply {
            type = bodyType
            bullet = bodyType == BodyDef.BodyType.DynamicBody
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
            setTransform(centerX, centerY, rotation.toRadians)
            polygonShape.dispose()
        }
    }

    fun clearChildren() {
        val actorsToRemove = Array<Actor>()
        group.children.forEach {
            actorsToRemove.add(it)
        }
        actorsToRemove.forEach {
            it.remove()
        }
        width = 0f
        height = 0f
        group.clearChildren()
    }

    override fun reset() {
        group.run {
            setPosition(0f, 0f)
            setOrigin(0f, 0f)
            setSize(0f, 0f)
            setScale(1f)
            rotation = 0f
            color = Color.WHITE
            name = ""
            zIndex = 0
            touchable = Touchable.enabled
            isVisible = true
            userObject = null
            debug = false
            remove()
            clear()
        }
    }

    companion object : ComponentResolver<Scene2DComponent>(Scene2DComponent::class.java)
}

val Entity.scene2D: Scene2DComponent
    get() = Scene2DComponent[this]


fun Entity.scene2D(context: Context) =
    add(createComponent<Scene2DComponent>(context).apply {
        userObject = this@scene2D
    })!!

fun Entity.scene2D(
    context: Context,
    ninePatch: NinePatch,
    centerX: Float = 0f, centerY: Float = 0f,
    width: Float = 0f, height: Float = 0f,
    rotation: Float = 0f
) = add(createComponent<Scene2DComponent>(context).apply {
    addNinePatch(context, ninePatch, centerX, centerY, width, height, rotation)
    userObject = this@scene2D
})!!

fun Entity.scene2D(
    context: Context,
    textureRegion: TextureRegion,
    centerX: Float = 0f, centerY: Float = 0f,
    width: Float = 0f, height: Float = 0f,
    rotation: Float = 0f
) =
    add(createComponent<Scene2DComponent>(context).apply {
        addImage(context, textureRegion, centerX, centerY, width, height, rotation)
        userObject = this@scene2D
    })!!

fun Entity.scene2D(
    context: Context,
    centerX: Float = 0f, centerY: Float = 0f,
    width: Float = 0f, height: Float = 0f,
    rotation: Float = 0f
) =
    add(createComponent<Scene2DComponent>(context).apply {
        this.width = width
        this.height = height
        this.centerX = centerX
        this.centerY = centerY
        this.rotation = rotation
    })!!
