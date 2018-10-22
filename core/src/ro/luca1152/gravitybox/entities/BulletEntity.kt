package ro.luca1152.gravitybox.entities

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import ktx.assets.getAsset
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.EntityCategory
import ro.luca1152.gravitybox.utils.GameStage
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class BulletEntity(playerEntity: PlayerEntity = Injekt.get(),
                   world: World = Injekt.get(),
                   stage: GameStage = Injekt.get(),
                   manager: AssetManager = Injekt.get()) : Entity() {
    companion object {
        const val SPEED = 10f
    }

    init {
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
        body.createFixture(bulletFixtureDef)
        add(PhysicsComponent(body))

        // ImageComponent
        add(ImageComponent(stage, manager.getAsset("graphics/bullet.png"), body.worldCenter.x, body.worldCenter.y))
        image.color = ColorScheme.darkColor
    }
}