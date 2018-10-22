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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World
import ktx.assets.getAsset
import ro.luca1152.gravitybox.components.ImageComponent
import ro.luca1152.gravitybox.components.PhysicsComponent
import ro.luca1152.gravitybox.components.PlayerComponent
import ro.luca1152.gravitybox.components.image
import ro.luca1152.gravitybox.utils.ColorScheme.darkColor
import ro.luca1152.gravitybox.utils.EntityCategory
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.MapBodyBuilder
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlayerEntity(map: TiledMap = Injekt.get(),
                   world: World = Injekt.get(),
                   stage: GameStage = Injekt.get(),
                   manager: AssetManager = Injekt.get()) : Entity() {
    init {
        // PlayerComponent
        add(PlayerComponent())

        // PhysicsComponent
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
        }
        val body = world.createBody(bodyDef)
        val playerObject = map.layers.get("Player").objects[0]
        val fixtureDef = FixtureDef().apply {
            shape = MapBodyBuilder.getRectangle(playerObject as RectangleMapObject)
            density = 1.15f
            friction = 2f
            filter.categoryBits = EntityCategory.PLAYER.bits
            filter.maskBits = EntityCategory.OBSTACLE.bits
        }
        body.userData = this
        body.createFixture(fixtureDef)
        add(PhysicsComponent(body))

        // ImageComponent
        add(ImageComponent(stage, manager.getAsset("graphics/player.png"), body.worldCenter.x, body.worldCenter.y))
        image.color = darkColor
    }
}