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
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.utils.ColorScheme.darkColor
import ro.luca1152.gravitybox.utils.EntityCategory
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class Bullet(private val world: World,
             player: Player,
             manager: AssetManager = Injekt.get()) : Image(manager.get("graphics/bullet.png", Texture::class.java)) {
    companion object {
        const val SPEED = 40f

        fun collisionWithWall(player: Player, body: Body,
                              manager: AssetManager = Injekt.get()) {
            // Play the collision sound
            manager.get("audio/bullet-wall-collision.wav", Sound::class.java).play(.4f)

            // Create the force vector
            val sourcePosition = Vector2(body.worldCenter.x, body.worldCenter.y)
            val distance = player.body.worldCenter.dst(sourcePosition)
            val forceVector = player.body.worldCenter.cpy().apply {
                sub(sourcePosition)
                nor()
                scl(13500f) // Multiply the force vector by an amount for a greater push

                // Take into account the distance between the source and the player
                // It's > 1 because you don't want to multiply the forceVector if the source is too close
                if (distance.toDouble() >= 1) {
                    scl(1f / Math.pow(distance.toDouble(), .7).toFloat())
                }
            }
            player.body.applyForce(forceVector, player.body.worldCenter, true) // Push the player
            player.stage.addActor(Explosion(body.worldCenter.x, body.worldCenter.y)) // Draw the explosion
        }
    }
 
    val body: Body

    init {
        // Set Actor properties
        setSize(.6f, .6f)
        setOrigin(width / 2f, height / 2f)

        // Create the BodyDef
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            bullet = true
            position.set(player.body.worldCenter.x, player.body.worldCenter.y)
        }

        // Create the body
        body = world.createBody(bodyDef).apply { gravityScale = .5f }
        val polygonShape = PolygonShape().apply { setAsBox(.15f, .15f) }
        val bulletFixtureDef = FixtureDef().apply {
            shape = polygonShape
            density = .2f
            filter.categoryBits = EntityCategory.BULLET.bits
            filter.maskBits = EntityCategory.OBSTACLE.bits
        }
        body.createFixture(bulletFixtureDef)
    }

    override fun act(delta: Float) {
        super.act(delta)

        // Update Actor properties
        setPosition(body.worldCenter.x - width / 2f, body.worldCenter.y - height / 2f)
        rotation = MathUtils.radiansToDegrees * body.transform.rotation
        color = darkColor

        // Remove the bullet if its Box2D body was removed
        val bodies = Array<Body>()
        world.getBodies(bodies)
        if (bodies.lastIndexOf(body, true) == -1)
            addAction(sequence(
                    delay(.01f),
                    removeActor()
            ))
    }
}
