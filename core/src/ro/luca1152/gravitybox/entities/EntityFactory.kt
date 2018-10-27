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

package ro.luca1152.gravitybox.entities

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import ktx.assets.getAsset
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.EntityCategory
import ro.luca1152.gravitybox.utils.GameStage
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object EntityFactory {
    fun createBullet(playerEntity: Entity,
                     world: World = Injekt.get(),
                     stage: GameStage = Injekt.get(),
                     manager: AssetManager = Injekt.get(),
                     engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // BulletComponent
        add(BulletComponent())

        // PhysicsComponent
        val playerBody = playerEntity.physics.body
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            bullet = true
            position.set(playerBody.worldCenter.x, playerBody.worldCenter.y)
        }
        val body = world.createBody(bodyDef).apply { gravityScale = .5f }
        val polygonShape = PolygonShape().apply { setAsBox(.15f, .15f) }
        val bulletFixtureDef = FixtureDef().apply {
            shape = polygonShape
            density = .2f
            filter.categoryBits = EntityCategory.BULLET.bits
            filter.maskBits = EntityCategory.OBSTACLE.bits
        }
        body.userData = this
        body.createFixture(bulletFixtureDef)
        add(PhysicsComponent(body))

        // ImageComponent
        add(ImageComponent(stage, manager.getAsset("graphics/bullet.png"), body.worldCenter.x, body.worldCenter.y))
        image.color = ColorScheme.currentDarkColor

        // ColorComponent
        add(ColorComponent(ColorType.DARK))
    }!!

    fun createExplosion(position: Vector2,
                        stage: GameStage = Injekt.get(),
                        manager: AssetManager = Injekt.get(),
                        engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // ExplosionComponent
        add(ExplosionComponent())

        // ImageComponent
        add(ImageComponent(stage, manager.getAsset("graphics/circle.png"), position.x, position.y))
        image.run {
            color = ColorScheme.currentDarkColor
            setScale(1f)
            addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.scaleBy(3f, 3f, .25f),
                            Actions.fadeOut(.25f, Interpolation.exp5)
                    ),
                    Actions.removeActor(),
                    Actions.run { engine.removeEntity(this@apply) }
            ))
        }

        // ColorComponent
        add(ColorComponent(ColorType.DARK))
    }!!

    fun createFinish(mapEntity: Entity,
                     stage: GameStage = Injekt.get(),
                     manager: AssetManager = Injekt.get(),
                     engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // FinishComponent
        add(FinishComponent())

        // PhysicsComponent
        val body = mapEntity.map.getFinishBody()
        add(PhysicsComponent(body))

        // CollisionBoxComponent
        add(CollisionBoxComponent(2f))

        // ImageComponent
        add(ImageComponent(stage, manager.getAsset("graphics/finish.png"), body.worldCenter.x, body.worldCenter.y))
        image.color = ColorScheme.currentDarkColor
        image.addAction(RepeatAction().apply {
            action = Actions.sequence(
                    Actions.fadeOut(1f),
                    Actions.fadeIn(1f)
            )
            count = RepeatAction.FOREVER
        })

        // ColorComponent
        add(ColorComponent(ColorType.DARK))
    }!!

    fun createMap(levelNumber: Int,
                  engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        add(MapComponent(levelNumber))
    }!!

    fun createPlatform(engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // PlatformComponent
        add(PlatformComponent())
    }!!

    fun createPlayer(mapEntity: Entity,
                     stage: GameStage = Injekt.get(),
                     manager: AssetManager = Injekt.get(),
                     engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // PlayerComponent
        add(PlayerComponent())

        // PhysicsComponent
        val body = mapEntity.map.getPlayerBody()
        add(PhysicsComponent(body))

        // CollisionBoxComponent
        add(CollisionBoxComponent(1f))

        // ImageComponent
        add(ImageComponent(stage, manager.getAsset("graphics/player.png"), body.worldCenter.x, body.worldCenter.y))
        image.color = ColorScheme.currentDarkColor

        // ColorComponent
        add(ColorComponent(ColorType.DARK))
    }!!
}