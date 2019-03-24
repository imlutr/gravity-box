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
import com.badlogic.gdx.utils.Pools
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object BulletEntity {
    private const val WIDTH = .15f
    private const val HEIGHT = .15f

    fun createEntity(
        x: Float, y: Float,
        manager: AssetManager = Injekt.get()
    ) = newEntity().apply {
        bullet()
        body(createBulletBody(x, y, this), this)
        image(manager.get(Assets.tileset).findRegion("bullet"), x, y)
        color(ColorType.DARK)
        addToEngine()
    }

    private fun createBulletBody(
        x: Float, y: Float,
        userData: Entity,
        world: World = Injekt.get()
    ): Body {
        val bodyDef = Pools.obtain(BodyDef::class.java).apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(x, y)
            bullet = true
            fixedRotation = false
        }
        val fixtureDef = FixtureDef().apply {
            shape = PolygonShape().apply {
                setAsBox(WIDTH, HEIGHT)
            }
            density = .2f
            filter.categoryBits = EntityCategory.BULLET.bits
            filter.maskBits = EntityCategory.PLATFORM.bits
        }
        return world.createBody(bodyDef).apply {
            Pools.free(bodyDef)
            createFixture(fixtureDef)
            fixtureDef.shape.dispose()
            gravityScale = .5f
            this.userData = userData
        }
    }
}