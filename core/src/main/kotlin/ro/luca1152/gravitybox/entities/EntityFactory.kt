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
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import ktx.assets.getAsset
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.removeAndResetEntity
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.Reference
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import ro.luca1152.gravitybox.utils.ui.ToggleButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get


object EntityFactory {
    fun createBullet(playerEntity: Entity, world: World = Injekt.get(),
                     manager: AssetManager = Injekt.get(), engine: PooledEngine = Injekt.get()) = engine.createEntity().apply label@{
        // BulletComponent
        add(engine.createComponent(BulletComponent::class.java))

        // BodyComponent
        add(engine.createComponent(BodyComponent::class.java))
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            bullet = true
            position.set(playerEntity.body.body.worldCenter)
        }
        val fixtureDef = FixtureDef().apply {
            shape = PolygonShape().apply { setAsBox(.15f, .15f) }
            density = .2f
            filter.categoryBits = EntityCategory.BULLET.bits
            filter.maskBits = EntityCategory.BULLET.bits
        }
        val body = world.createBody(bodyDef).apply {
            createFixture(fixtureDef)
            gravityScale = .5f
            userData = this@label
        }
        fixtureDef.shape.dispose()
        this.body.set(body, this)

        // ImageComponent
        add(engine.createComponent(ImageComponent::class.java))
        this.image.set(manager.getAsset("graphics/bullet.png"), body.worldCenter)

        // ColorComponent
        add(engine.createComponent(ColorComponent::class.java))
        this.color.colorType = ColorType.DARK
        this.image.color = ColorScheme.currentDarkColor

        engine.addEntity(this)
    }!!

    fun createExplosionImage(position: Vector2,
                             manager: AssetManager = Injekt.get(),
                             engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // ExplosionComponent
        add(engine.createComponent(ExplosionComponent::class.java))

        // ImageComponent
        add(engine.createComponent(ImageComponent::class.java))
        this.image.set(manager.getAsset("graphics/circle.png"), position)
        this.image.img.run {
            setScale(1f)
            addAction(
                    Actions.sequence(
                            Actions.parallel(
                                    Actions.scaleBy(3f, 3f, .25f),
                                    Actions.fadeOut(.25f, Interpolation.exp5)
                            ),
                            Actions.run { engine.removeAndResetEntity(this@apply) },
                            Actions.removeActor()
                    )
            )
        }

        // ColorComponent
        add(engine.createComponent(ColorComponent::class.java))
        this.color.set(ColorType.DARK)
        this.image.color = ColorScheme.currentDarkColor

        engine.addEntity(this)
    }!!

    fun createFinish(body: Body,
                     manager: AssetManager = Injekt.get(),
                     engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // FinishComponent
        add(engine.createComponent(FinishComponent::class.java))

        // BodyComponent
        add(engine.createComponent(BodyComponent::class.java))
        this.body.set(body, this)

        // CollisionBoxComponent
        add(engine.createComponent(CollisionBoxComponent::class.java))
        this.collisionBox.set(2f)

        // ImageComponent
        add(engine.createComponent(ImageComponent::class.java))
        this.image.set(manager.getAsset("graphics/finish.png"), this.body.body.worldCenter)
        this.image.img.run {
            addAction(RepeatAction().apply {
                action = Actions.sequence(
                        Actions.fadeOut(1f),
                        Actions.fadeIn(1f)
                )
                count = RepeatAction.FOREVER
            })
        }

        // ColorComponent
        add(engine.createComponent(ColorComponent::class.java))
        this.color.set(ColorType.DARK)
        this.image.color = ColorScheme.currentDarkColor

        engine.addEntity(this)
    }!!

    fun createMap(levelNumber: Int, engine: PooledEngine = Injekt.get()) =
            engine.createEntity().apply {
                // MapComponent
                add(engine.createComponent(MapComponent::class.java))
                this.map.set(levelNumber)
            }!!

    fun createPlatforms(platforms: ArrayList<Pair<Body, MapObject>>,
                        engine: PooledEngine = Injekt.get()) {
        for (platform in platforms) {
            engine.createEntity().apply {
                // MapObjectComponent
                add(engine.createComponent(MapObjectComponent::class.java))
                this.mapObject.set(platform.second)

                // PlatformComponent
                add(engine.createComponent(PlatformComponent::class.java))
                this.platform.isDynamic = platform.first.userData as Boolean

                // BodyComponent
                add(engine.createComponent(BodyComponent::class.java))
                this.body.set(platform.first, this)
                engine.addEntity(this)
            }
        }
    }

    fun createPlayer(body: Body,
                     manager: AssetManager = Injekt.get(),
                     engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // PlayerComponent
        add(engine.createComponent(PlayerComponent::class.java))

        // BodyComponent
        add(engine.createComponent(BodyComponent::class.java))
        this.body.set(body, this)

        // CollisionBoxComponent
        add(engine.createComponent(CollisionBoxComponent::class.java))
        this.collisionBox.set(1f)

        // ImageComponent
        add(engine.createComponent(ImageComponent::class.java))
        this.image.set(manager.getAsset("graphics/player.png"), this.body.body.worldCenter)

        // ColorComponent
        add(engine.createComponent(ColorComponent::class.java))
        this.color.set(ColorType.DARK)
        this.image.color = ColorScheme.currentDarkColor

        engine.addEntity(this)
    }!!

    fun createPoints(bodies: ArrayList<Body>, engine: PooledEngine = Injekt.get()) {
        for (body in bodies) {
            engine.createEntity().apply {
                // PointComponent
                add(engine.createComponent(PointComponent::class.java))

                // BodyComponent
                add(engine.createComponent(BodyComponent::class.java))
                this.body.set(body, this)

                // CollisionBoxComponent
                add(engine.createComponent(CollisionBoxComponent::class.java))
                this.collisionBox.set(1f)

                engine.addEntity(this)
            }
        }
    }

    fun createInputEntity(toggledButton: Reference<ToggleButton>,
                          engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        // InputComponent
        add(engine.createComponent(InputComponent::class.java)).run {
            input.set(toggledButton)
        }

        engine.addEntity(this)
    }!!

    fun createDebugEntity(engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        add(engine.createComponent(DebugComponent::class.java))
        engine.addEntity(this)
    }!!

    fun createUndoRedoEntity(engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        add(engine.createComponent(UndoRedoComponent::class.java))
        engine.addEntity(this)
    }!!
}