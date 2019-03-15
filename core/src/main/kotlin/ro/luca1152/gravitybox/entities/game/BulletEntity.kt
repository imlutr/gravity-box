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

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.screens.Assets
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object BulletEntity {
    private const val WIDTH = .15f
    private const val HEIGHT = .15f

    fun createEntity(
        x: Float, y: Float,
        world: World = Injekt.get(),
        manager: AssetManager = Injekt.get(),
        engine: PooledEngine = Injekt.get()
    ) = engine.createEntity().apply label@{
        add(engine.createComponent(BulletComponent::class.java))
        add(engine.createComponent(BodyComponent::class.java)).run {
            val bodyDef = BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
                bullet = true
                position.set(x, y)
            }
            val fixtureDef = FixtureDef().apply {
                shape = PolygonShape().apply {
                    setAsBox(WIDTH, HEIGHT)
                }
                density = .2f
                filter.categoryBits = EntityCategory.BULLET.bits
                filter.maskBits = EntityCategory.PLATFORM.bits
            }
            val body = world.createBody(bodyDef).apply {
                createFixture(fixtureDef)
                fixtureDef.shape.dispose()
                gravityScale = .5f
                userData = this@label
            }
            this.body.set(body, this)
        }
        add(engine.createComponent(ImageComponent::class.java)).run {
            image.set(manager.get(Assets.tileset).findRegion("bullet"), x, y)
        }
        add(engine.createComponent(ColorComponent::class.java)).run {
            color.colorType = ColorType.DARK
        }
        engine.addEntity(this)
    }!!
}