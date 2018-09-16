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
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.assets.getAsset
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.screens.font
import ro.luca1152.gravitybox.screens.fontShader
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


val Int.pixelsToMeters: Float
    get() = this / Level.PPM

class Level(levelNumber: Int,
            manager: AssetManager = Injekt.get(),
            private val batch: Batch = Injekt.get(),
            private val playScreen: PlayScreen = Injekt.get()) {
    companion object {
        const val TOTAL_LEVELS = 10
        const val PPM = 32f // Pixels per meter
    }

    private val map: TiledMap
    private val mapWidth: Int
    private val mapHeight: Int
    private val mapHue: Int
    private val world: World
    private val finish: Finish
    val player: Player

    private val originalLightColor: Color
    private val originalDarkColor: Color

    private var mapIsVisible = false
    var isFinished = false

    private val labelStyle: Label.LabelStyle
    private val mapRenderer: OrthogonalTiledMapRenderer
    private val b2dRenderer: Box2DDebugRenderer
    private val uiStage: Stage
    val stage: Stage

    init {
        map = manager.getAsset("maps/map-$levelNumber.tmx")
        val mapProperties = map.properties
        mapWidth = mapProperties.get("width") as Int
        mapHeight = mapProperties.get("height") as Int
        world = World(Vector2(0f, -72f), true)
        MapBodyBuilder.buildShapes(map, PPM, world)
        finish = Finish(map, world)
        player = Player(map, world)

        mapHue = mapProperties.get("hue") as Int
        lightColor = ColorScheme.getLightColor(mapHue)
        darkColor = ColorScheme.getDarkColor(mapHue)
        originalLightColor = lightColor.cpy()
        originalDarkColor = darkColor.cpy()
        lightColor2 = ColorScheme.getLightColor2(mapHue)
        darkColor2 = ColorScheme.getDarkColor2(mapHue)

        stage = Stage(ExtendViewport(720 / 64f, 1280 / 64f), batch)
        (stage.camera as OrthographicCamera).zoom = 2f // Don't change this or the camera won't align correctly :\
        uiStage = Stage(ExtendViewport(720f, 1280f), stage.batch)
        b2dRenderer = Box2DDebugRenderer()
        labelStyle = Label.LabelStyle(font, darkColor)
        mapRenderer = OrthogonalTiledMapRenderer(map, 1 / PPM, batch)

        stage.addActor(finish.apply { isVisible = false })
        stage.addActor(player.apply { isVisible = false })

        setInputProcessor()
        setContactListener()
        showLevelLabel(levelNumber)
        if (levelNumber == Level.TOTAL_LEVELS) showFinishMessage()
    }

    private fun setInputProcessor() {
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                // Create the bullet
                val bullet = Bullet(world, player)
                stage.addActor(bullet)

                val worldCoordinates = Vector3(screenX.toFloat(), screenY.toFloat(), 0f).run {
                    stage.camera.unproject(this)
                }
                val forceVector = player.body.worldCenter.cpy().apply {
                    sub(worldCoordinates.x, worldCoordinates.y)
                    nor()
                    scl(-Bullet.SPEED)
                }
                bullet.body.linearVelocity = forceVector
                return true
            }

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.R) {
                    player.restart = true
                }
                return true
            }
        }
    }

    private fun setContactListener() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                val bodyA = contact.fixtureA.body
                val bodyB = contact.fixtureB.body

                // Collision between a bullet and a wall
                when {
                    contact.fixtureB.filterData.categoryBits == EntityCategory.BULLET.bits -> flagForDelete(bodyB)
                    contact.fixtureA.filterData.categoryBits == EntityCategory.BULLET.bits -> flagForDelete(bodyA)
                }
            }

            private fun flagForDelete(body: Body) {
                val userData = MyUserData().apply { isFlaggedForDelete = true }
                body.userData = userData
            }

            override fun endContact(contact: Contact) {}

            override fun preSolve(contact: Contact, oldManifold: Manifold) {}

            override fun postSolve(contact: Contact, impulse: ContactImpulse) {}
        })
    }

    private fun showLevelLabel(levelNumber: Int) {
        uiStage.addActor(Label("#$levelNumber", labelStyle).apply {
            setAlignment(Align.right)
            setPosition(uiStage.width - prefWidth - 10f, 7f)
            // Add fadeIn effect if it's the first level
            if (levelNumber == 1) {
                addAction(Actions.fadeOut(0f))
                addAction(Actions.fadeIn(2f))
            }
        })
    }

    private fun showFinishMessage() {
        playScreen.finishTimer = (playScreen.finishTimer * 100).toInt() / 100f
        uiStage.addActor(Label("Good job!\nYou finished the game in ${playScreen.finishTimer}s!", labelStyle).apply {
            setAlignment(Align.center)
            setPosition(uiStage.width / 2f - prefWidth / 2f, 700f)
        })
    }

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

    private fun updateVisibility() {
        // They are initially invisible because when you would restart the level they would flash for 0.01s in one corner
        player.isVisible = true
        finish.isVisible = true
        mapIsVisible = true
    }

    private fun updateCamera() {
        val zoom = (stage.camera as OrthographicCamera).zoom
        stage.camera.position.set(player.x + player.width / 2f, player.y + player.height / 2f, 0f)
        fixBounds(zoom)
        stage.camera.update()
    }

    private fun fixBounds(zoom: Float) {
        var mapLeft = 0f
        var mapRight = mapWidth * zoom
        if (mapWidth * zoom > stage.camera.viewportWidth * zoom) {
            mapLeft = (-1) * zoom
            mapRight = (mapWidth + 1) * zoom
        }
        val mapBottom = 0 * zoom
        val mapTop = mapHeight * zoom
        val cameraHalfWidth = stage.camera.viewportWidth / 2f * zoom
        val cameraHalfHeight = stage.camera.viewportHeight / 2f * zoom
        val cameraLeft = stage.camera.position.x - cameraHalfWidth
        val cameraRight = stage.camera.position.x + cameraHalfWidth
        val cameraBottom = stage.camera.position.y - cameraHalfHeight
        val cameraTop = stage.camera.position.y + cameraHalfHeight

        // Clamp horizontal axis
        when {
            stage.camera.viewportWidth * zoom > mapRight -> stage.camera.position.x = mapRight / 2f
            cameraLeft <= mapLeft -> stage.camera.position.x = mapLeft + cameraHalfWidth
            cameraRight >= mapRight -> stage.camera.position.x = mapRight - cameraHalfWidth
        }

        // Clamp vertical axis
        when {
            stage.camera.viewportHeight * zoom > mapTop -> stage.camera.position.y = mapTop / 2f
            cameraBottom <= mapBottom -> stage.camera.position.y = mapBottom + cameraHalfHeight
            cameraTop >= mapTop -> stage.camera.position.y = mapTop - cameraHalfHeight
        }
    }

    private fun sweepDeadBodies() {
        val array = Array<Body>()
        world.getBodies(array)
        for (body in array) {
            if (body != null && body.userData != null && body.userData.javaClass == MyUserData::class.java) {
                if ((body.userData as MyUserData).isFlaggedForDelete) {
                    Bullet.collisionWithWall(player, body)
                    world.destroyBody(body)
                    body.userData = null
                }
            }
        }
    }

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

    fun draw() {
        batch.color = darkColor
        batch.projectionMatrix = stage.camera.combined
        if (mapIsVisible) {
            mapRenderer.render()
            stage.draw()
//            b2dRenderer.render(world, stage.getCamera().combined);
            batch.color = Color.WHITE

        }
        uiStage.batch.shader = fontShader
        uiStage.draw()
        uiStage.batch.shader = null
    }
}
