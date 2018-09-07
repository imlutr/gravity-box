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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.FitViewport
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.ColorScheme.darkColor
import ro.luca1152.gravitybox.utils.ColorScheme.darkColor2
import ro.luca1152.gravitybox.utils.ColorScheme.lightColor
import ro.luca1152.gravitybox.utils.ColorScheme.lightColor2
import ro.luca1152.gravitybox.utils.EntityCategory
import ro.luca1152.gravitybox.utils.MapBodyBuilder
import ro.luca1152.gravitybox.utils.MyUserData
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class Level(levelNumber: Int,
            private val batch: Batch = Injekt.get(),
            manager: AssetManager = Injekt.get()) {
    // Map info
    private val map: TiledMap
    private val mapWidth: Int
    private val mapHeight: Int
    private val mapHue: Int
    private val world: World
    private val player: Player
    private val finish: Finish
    // Original colors
    private val originalLightColor: Color
    private val originalDarkColor: Color
    // Variables
    var isFinished = false
    var reset = false
    var mapIsVisible = false
    // Util
    private var labelStyle: Label.LabelStyle
    private val mapRenderer: OrthogonalTiledMapRenderer
    private val b2dRenderer: Box2DDebugRenderer
    var stage: Stage
    var uiStage: Stage

    init {
        // Load the map
        map = manager.get("maps/map-$levelNumber.tmx", TiledMap::class.java)
        val mapProperties = map.properties
        mapWidth = mapProperties.get("width") as Int
        mapHeight = mapProperties.get("height") as Int
        // Create the Box2D world
        world = World(Vector2(0f, -36f), true)
        MapBodyBuilder.buildShapes(map, MyGame.PPM, world)
        // Create the finish point and the player based on their position on the [map]
        finish = Finish(map, world)
        player = Player(map, world)

        // Generate colors
        mapHue = mapProperties.get("hue") as Int
        lightColor = ColorScheme.getLightColor(mapHue)
        darkColor = ColorScheme.getDarkColor(mapHue)
        originalLightColor = lightColor.cpy()
        originalDarkColor = darkColor.cpy()
        lightColor2 = ColorScheme.getLightColor2(mapHue)
        darkColor2 = ColorScheme.getDarkColor2(mapHue)

        // Initialize utils
        stage = Stage(FitViewport(20f, 20f), batch)
        uiStage = Stage(FitViewport(640f, 640f), stage.batch)
        b2dRenderer = Box2DDebugRenderer()
        labelStyle = Label.LabelStyle(MyGame.font32, darkColor)
        mapRenderer = OrthogonalTiledMapRenderer(map, 1 / MyGame.PPM, batch)

        // Add actors to stage
        finish.isVisible = false
        stage.addActor(finish)
        player.isVisible = false
        stage.addActor(player)

        // Misc
        setInputProcessor()
        setContactListener()
        if (levelNumber == 1) showHelpLabel()
        showLevelLabel(levelNumber)
        if (levelNumber == MyGame.TOTAL_LEVELS)
            showFinishMessage()
    }

    /**
     * Handles the mouse click.
     */
    private fun setInputProcessor() {
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                // Create the bullet
                val bullet = Bullet(world, player)
                stage.addActor(bullet)

                val worldCoordinates = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
                stage.camera.unproject(worldCoordinates)
                val sourcePosition = Vector2(worldCoordinates.x, worldCoordinates.y)
                val forceVector = player.body.worldCenter.cpy()
                forceVector.sub(sourcePosition)
                forceVector.nor()
                forceVector.scl(-Bullet.SPEED)
                bullet.body.linearVelocity = forceVector
                return true
            }

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.R) {
                    reset = true
                }
                return true
            }
        }
    }

    /**
     * Removes bullets when they collide with the walls.
     */
    private fun setContactListener() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                val bodyA = contact.fixtureA.body
                val bodyB = contact.fixtureB.body

                // Collision between a bullet and a wall
                if (contact.fixtureB.filterData.categoryBits == EntityCategory.BULLET.bits)
                    flagForDelete(bodyB)
                if (contact.fixtureA.filterData.categoryBits == EntityCategory.BULLET.bits)
                    flagForDelete(bodyA)
            }

            private fun flagForDelete(body: Body) {
                val userData = MyUserData()
                userData.isFlaggedForDelete = true
                body.userData = userData
            }

            override fun endContact(contact: Contact) {}

            override fun preSolve(contact: Contact, oldManifold: Manifold) {}

            override fun postSolve(contact: Contact, impulse: ContactImpulse) {}
        })
    }

    /**
     * Shows how to play the game and the keymap
     */
    private fun showHelpLabel() {
        val info1 = Label("shoot at the walls/floor to move\npress 'R' to restart the level", labelStyle)
        info1.setAlignment(Align.center)
        info1.setPosition(320 - info1.prefWidth / 2f, 470f)
        info1.addAction(Actions.fadeOut(0f))
        info1.addAction(Actions.fadeIn(2f))
        uiStage.addActor(info1)

        val info2 = Label("the blinking object is the finish point", labelStyle)
        info2.setAlignment(Align.center)
        info2.setPosition(320 - info2.prefWidth / 2f, 135f)
        info2.addAction(Actions.fadeOut(0f))
        info2.addAction(Actions.fadeIn(2f))
        uiStage.addActor(info2)
    }

    /**
     * Shows the level number in the bottom right.
     */
    private fun showLevelLabel(levelNumber: Int) {
        val level = Label("#$levelNumber", labelStyle)
        level.setAlignment(Align.right)
        level.setPosition(640f - level.prefWidth - 10f, 7f)
        if (levelNumber == 1) {
            level.addAction(Actions.fadeOut(0f))
            level.addAction(Actions.fadeIn(2f))
        }
        uiStage.addActor(level)
    }

    /**
     * Shows in how much time the game was finished.
     */
    private fun showFinishMessage() {
        PlayScreen.timer = (PlayScreen.timer * 100).toInt() / 100f
        val finish = Label("Good job!\nYou finished the game\nin " + PlayScreen.timer + "s!", labelStyle)
        finish.setAlignment(Align.center)
        finish.setPosition(320f - finish.prefWidth / 2f, 350f)
        uiStage.addActor(finish)
    }

    /**
     * Is called every frame.
     */
    fun update(delta: Float) {
        mapRenderer.setView(stage.camera as OrthographicCamera)
        world.step(1 / 60f, 6, 2)
        stage.act(delta)
        uiStage.act(delta)
        updateVisibility()
        updateCamera()
        sweepDeadBodies()
        playerCollidesFinish()
    }

    /**
     * Sets every actor to be visible. Initially they are invisible because when you restarted the level they would show up for 1ms.
     */
    private fun updateVisibility() {
        player.isVisible = true
        finish.isVisible = true
        mapIsVisible = true
    }

    /**
     * Keeps the camera within the map's bounds.
     */
    private fun updateCamera() {
        stage.camera.position.set(player.x, player.y - 5f, 0f)
        val mapLeft = -1
        val mapRight = mapWidth + 1
        val mapBottom = 0
        val mapTop = mapHeight
        val cameraHalfWidth = stage.camera.viewportWidth * .5f
        val cameraHalfHeight = stage.camera.viewportHeight * .5f
        val cameraLeft = stage.camera.position.x - cameraHalfWidth
        val cameraRight = stage.camera.position.x + cameraHalfWidth
        val cameraBottom = stage.camera.position.y - cameraHalfHeight
        val cameraTop = stage.camera.position.y + cameraHalfHeight
        // Clamp horizontal axis
        when {
            stage.camera.viewportWidth > mapRight -> stage.camera.position.x = (mapRight / 2).toFloat()
            cameraLeft <= mapLeft -> stage.camera.position.x = mapLeft + cameraHalfWidth
            cameraRight >= mapRight -> stage.camera.position.x = mapRight - cameraHalfWidth
        }
        // Clamp vertical axis
        when {
            stage.camera.viewportHeight > mapTop -> stage.camera.position.y = (mapTop / 2).toFloat()
            cameraBottom <= mapBottom -> stage.camera.position.y = mapBottom + cameraHalfHeight
            cameraTop >= mapTop -> stage.camera.position.y = mapTop - cameraHalfHeight
        }
        // Update the camera
        stage.camera.update()
    }

    /**
     * Destroys every bullet that collided with a wall (checked in contactListener).
     * You can't destroy bodies from the contactListener, so I did it here.
     */
    private fun sweepDeadBodies() {
        val array = Array<Body>()
        world.getBodies(array)
        for (body in array) {
            if (body != null && body.userData != null && body.userData.javaClass == MyUserData::class.java) {
                val data = body.userData as MyUserData
                if (data.isFlaggedForDelete) {
                    Bullet.collisionWithWall(player, body)
                    world.destroyBody(body)
                    body.userData = null
                }
            }
        }
    }

    /**
     * If the player is in the finish point, transition to the secondary color scheme.
     * If the player leaves it, transition back.
     */
    private fun playerCollidesFinish() {
        if (player.collisionBox.overlaps(finish.collisionBox)) {
            lightColor.lerp(lightColor2, .05f)
            darkColor.lerp(darkColor2, .05f)
        } else {
            lightColor.lerp(originalLightColor, .05f)
            darkColor.lerp(originalDarkColor, .05f)
        }

        // If the two colors are close enough (inconsistency caused by lerp), advance to the next level
        if (Math.abs(lightColor.r - lightColor2.r) <= 3f / 255f && Math.abs(lightColor.g - lightColor2.g) <= 3f / 255f && Math.abs(lightColor.b - lightColor2.b) <= 3f / 255f) {
            isFinished = true
        }
    }

    /**
     * Draws every platform from the level, the player and the finish point.
     */
    fun draw() {
        batch.color = darkColor
        batch.projectionMatrix = stage.camera.combined
        if (mapIsVisible) {
            mapRenderer.render()
            stage.draw()
            batch.color = Color.WHITE
//            b2dRenderer.render(world, stage.getCamera().combined);
        }
        uiStage.draw()
    }
}
