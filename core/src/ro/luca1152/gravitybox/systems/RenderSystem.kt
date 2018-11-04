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

package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import ro.luca1152.gravitybox.PPM
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.tryGet
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.GameCamera
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Used to render everything on the screen, excluding the UI.
 */
class RenderSystem(
    private val mapEntity: Entity,
    private val world: World = Injekt.get(),
    private val stage: GameStage = Injekt.get(),
    private val batch: Batch = Injekt.get(),
    private val shapeRenderer: ShapeRenderer = Injekt.get(),
    private val gameCamera: GameCamera = Injekt.get(),
    private val gameViewport: GameViewport = Injekt.get()
) : EntitySystem() {
    private val mapRenderer = OrthogonalTiledMapRenderer(mapEntity.map.tiledMap, 1 / PPM, batch)
    private val b2DDebugRenderer = Box2DDebugRenderer()

    override fun update(deltaTime: Float) {
        stage.act()
        gameViewport.apply()
        stage.batch.projectionMatrix = gameCamera.combined
        shapeRenderer.setAutoShapeType(true)
        Gdx.gl20.glLineWidth(10f)

//        drawImages()
//        drawTiledMap()
        drawPhysicsDebug()
    }

    private fun drawImages() {
        /**
         * Reposition the actors so they are drawn from the center (Box2D bodies' position is from center, not bottom-left)
         * It subtracts half the size of the images and adds it back when restoring.
         */
        fun repositionImages(restore: Boolean = false) {
            for (i in 0 until stage.actors.size) {
                val change = if (restore) 1 else -1
                stage.actors[i].x += change * stage.actors[i].width / 2f
                stage.actors[i].y += change * stage.actors[i].height / 2f
            }
        }

        repositionImages()
        stage.draw()
        repositionImages(restore = true)
    }

    private fun drawTiledMap() {
        // Update the map in case the level changed
        mapRenderer.map = mapEntity.map.tiledMap

        // Make the platforms from the map have a color from the color scheme
        batch.color = ColorScheme.currentDarkColor
        mapRenderer.setView(gameCamera)
        mapRenderer.render()
        batch.color = Color.WHITE
    }

    private fun drawPhysicsDebug() {
        fun drawXAtOrigins() {
            shapeRenderer.set(ShapeRenderer.ShapeType.Line)
            for (entity in engine.getEntitiesFor(Family.all(PhysicsComponent::class.java).get())) {
                val body = entity.physics.body
                if (body.type == BodyDef.BodyType.DynamicBody) {
                    (body.userData as Entity).run {
                        when {
                            this.tryGet(FinishComponent) != null -> shapeRenderer.color = Color.LIME
                            this.tryGet(PointComponent) != null -> shapeRenderer.color = Color.BLUE
                            this.tryGet(PlayerComponent) != null -> shapeRenderer.color = Color.RED
                            this.tryGet(BulletComponent) != null -> shapeRenderer.color = Color.YELLOW
                        }
                    }
                    shapeRenderer.x(body.worldCenter, .05f)
                }
            }
        }

        fun drawPlatforms() {
            shapeRenderer.color = ColorScheme.currentDarkColor
            for (entity in engine.getEntitiesFor(Family.all(PlatformComponent::class.java, MapObjectComponent::class.java).get())) {
                when (entity.platform.isDynamic) {
                    true -> shapeRenderer.set(ShapeRenderer.ShapeType.Line)
                    false -> shapeRenderer.set(ShapeRenderer.ShapeType.Filled)
                }
                shapeRenderer.rect(
                    entity.mapObject.position.x,
                    entity.mapObject.position.y,
                    entity.mapObject.width,
                    entity.mapObject.height
                )
            }
        }
        b2DDebugRenderer.render(world, gameCamera.combined)
        shapeRenderer.projectionMatrix = gameCamera.combined
        shapeRenderer.begin()
        drawXAtOrigins()
        drawPlatforms()
        shapeRenderer.end()
    }
}