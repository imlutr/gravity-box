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

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.Map
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme.darkColor
import ro.luca1152.gravitybox.utils.EntityCategory
import ro.luca1152.gravitybox.utils.MapBodyBuilder
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class Player(sourceMap: Map,
             destinationWorld: World,
             manager: AssetManager = Injekt.get()) : Image(manager.get("graphics/player.png", Texture::class.java)) {
    var body: Body
    val collisionBox: Rectangle
        get() {
            field.setPosition(x, y)
            return field
        }

    init {
        setSize(32 / MyGame.PPM, 32 / MyGame.PPM)
        setOrigin(width / 2f, height / 2f)

        // Read the object from the map
        val playerObject = sourceMap.layers.get("Player").objects.get(0)

        // Create the body definition
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody

        // Create the body
        body = destinationWorld.createBody(bodyDef)
        val fixtureDef = FixtureDef()
        fixtureDef.shape = MapBodyBuilder.getRectangle(playerObject as RectangleMapObject)
        fixtureDef.density = 2f
        fixtureDef.friction = 2f
        fixtureDef.filter.categoryBits = EntityCategory.PLAYER.bits
        fixtureDef.filter.maskBits = EntityCategory.OBSTACLE.bits
        body.createFixture(fixtureDef)

        // Create the collision box
        collisionBox = Rectangle()
        collisionBox.setSize(width, height)

        // Update the position
        setPosition(body.worldCenter.x - width / 2f, body.worldCenter.y - height / 2f)
    }

    override fun act(delta: Float) {
        super.act(delta)
        setPosition(body.worldCenter.x - width / 2f, body.worldCenter.y - height / 2f)
        rotation = MathUtils.radiansToDegrees * body.transform.rotation
        color = darkColor
    }
}
