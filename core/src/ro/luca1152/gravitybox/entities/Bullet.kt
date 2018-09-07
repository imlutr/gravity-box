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

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.MyGame

class Bullet(private val world: World, player: Player) : Image(MyGame.manager.get("graphics/bullet.png", Texture::class.java)) {

    var body: Body

    init {
        setSize(.3f, .3f)
        setOrigin(width / 2f, height / 2f)
        val bodyDef = BodyDef()
        bodyDef.type = BodyDef.BodyType.DynamicBody
        bodyDef.bullet = true
        bodyDef.position.set(player.body.worldCenter.x, player.body.worldCenter.y)
        body = world.createBody(bodyDef)
        body.setGravityScale(0.5f)
        val polygonShape = PolygonShape()
        polygonShape.setAsBox(.15f, .15f)
        val bulletFixtureDef = FixtureDef()
        bulletFixtureDef.shape = polygonShape
        bulletFixtureDef.density = .2f
        bulletFixtureDef.filter.categoryBits = MyGame.EntityCategory.BULLET.bits
        bulletFixtureDef.filter.maskBits = MyGame.EntityCategory.OBSTACLE.bits
        body.createFixture(bulletFixtureDef)
    }

    override fun act(delta: Float) {
        super.act(delta)
        setPosition(body.worldCenter.x - width / 2f, body.worldCenter.y - height / 2f)
        rotation = MathUtils.radiansToDegrees * body.transform.rotation
        color = MyGame.darkColor

        // Remove the actor if the body was removed
        val bodies = Array<Body>()
        world.getBodies(bodies)
        if (bodies.lastIndexOf(body, true) == -1)
            addAction(Actions.sequence(
                    Actions.delay(.01f),
                    Actions.removeActor()
            ))
    }

    companion object {
        val SPEED = 20f

        // Move the player
        fun collisionWithWall(player: Player, body: Body) {
            MyGame.manager.get("audio/bullet-wall-collision.wav", Sound::class.java).play(.4f)

            // Create the force vector
            val sourcePosition = Vector2(body.worldCenter.x, body.worldCenter.y)
            val distance = player.body.worldCenter.dst(sourcePosition)
            val forceVector = player.body.worldCenter.cpy()
            forceVector.sub(sourcePosition)
            forceVector.nor()
            forceVector.scl(2800f) // Multiply the force vector by an amount for a greater push
            // Take into account the distance between the source and the player
            // It's > 1 because you don't want to multiply the forceVector if the source is too close
            if (Math.pow(distance.toDouble(), 1.7).toFloat() > 1) {
                forceVector.scl(1f / Math.pow(distance.toDouble(), 1.7).toFloat())
            }
            // Push the player
            player.body.applyForce(forceVector, player.body.worldCenter, true)

            // Create explosion
            player.stage.addActor(Explosion(body.worldCenter.x, body.worldCenter.y))
        }
    }
}
