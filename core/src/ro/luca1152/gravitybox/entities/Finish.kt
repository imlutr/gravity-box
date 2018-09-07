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

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.Map
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.MapBodyBuilder

class Finish(sourceMap: Map, destinationWorld: World) : Image(MyGame.manager.get("graphics/finish.png", Texture::class.java)) {
    var body: Body
    var collisionBox: Rectangle
        get() {
            field.setPosition(x, y)
            return field
        }

    init {
        setSize(64 / MyGame.PPM, 64 / MyGame.PPM)
        setOrigin(width / 2f, height / 2f)

        // Read the object form the map
        val finishObject = sourceMap.layers.get("Finish").objects.get(0)

        // Create the body definition
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody

        // Create the body
        body = destinationWorld.createBody(bodyDef)
        body.gravityScale = 0f
        val fixtureDef = FixtureDef()
        fixtureDef.shape = MapBodyBuilder.getRectangle(finishObject as RectangleMapObject)
        fixtureDef.density = 100f
        fixtureDef.filter.categoryBits = MyGame.EntityCategory.FINISH.bits
        fixtureDef.filter.maskBits = MyGame.EntityCategory.NONE.bits
        body.createFixture(fixtureDef)

        // Update the position
        setPosition(body.worldCenter.x - width / 2f, body.worldCenter.y - height / 2f)

        // Add permanent blinking effect
        val repeatAction = RepeatAction()
        repeatAction.action = Actions.sequence(
                Actions.fadeOut(1f),
                Actions.fadeIn(1f)
        )
        repeatAction.count = RepeatAction.FOREVER
        addAction(repeatAction)

        // Create the collision box
        collisionBox = Rectangle()
        collisionBox.setSize(width, height)

        // Update the position
        setPosition(body.worldCenter.x - width / 2f, body.worldCenter.y - height / 2f)
    }// Create the image

    override fun act(delta: Float) {
        super.act(delta)
        setPosition(body.worldCenter.x - width / 2f, body.worldCenter.y - height / 2f)
        color.r = MyGame.darkColor.r
        color.g = MyGame.darkColor.g
        color.b = MyGame.darkColor.b
    }
}
