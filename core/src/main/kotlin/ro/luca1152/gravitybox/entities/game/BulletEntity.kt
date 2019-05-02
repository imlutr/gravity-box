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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.*
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity

object BulletEntity {
    private const val WIDTH = .15f
    private const val HEIGHT = .15f
    private val CATEGORY_BITS = EntityCategory.BULLET.bits
    private val MASK_BITS = EntityCategory.PLATFORM.bits

    fun createEntity(
        context: Context,
        x: Float, y: Float
    ) = newEntity(context).apply {
        val manager: AssetManager = context.inject()
        bullet(context)
        body(context, createBulletBody(context, x, y, this), CATEGORY_BITS, MASK_BITS)
        scene2D(context, manager.get(Assets.tileset).findRegion("bullet"), x, y)
        color(context, ColorType.DARK)
        addToEngine(context)
    }

    private fun createBulletBody(
        context: Context,
        x: Float, y: Float,
        userData: Entity
    ): Body {
        val world: World = context.inject()
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(x, y)
            bullet = true
        }
        val polygonShape = PolygonShape().apply {
            setAsBox(WIDTH, HEIGHT)
        }
        val fixtureDef = FixtureDef().apply {
            shape = polygonShape
            density = .2f
            filter.categoryBits = EntityCategory.BULLET.bits
            filter.maskBits = EntityCategory.PLATFORM.bits
        }
        return world.createBody(bodyDef).apply {
            createFixture(fixtureDef)
            polygonShape.dispose()
            gravityScale = .5f
            this.userData = userData
        }
    }
}