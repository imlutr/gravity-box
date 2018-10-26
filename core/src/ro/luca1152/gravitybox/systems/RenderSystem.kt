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

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import ktx.collections.GdxArray
import ro.luca1152.gravitybox.PPM
import ro.luca1152.gravitybox.components.map
import ro.luca1152.gravitybox.entities.MapEntity
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.GameCamera
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class RenderSystem(private val mapEntity: MapEntity = Injekt.get(),
                   private val world: World = Injekt.get(),
                   private val stage: GameStage = Injekt.get(),
                   private val batch: Batch = Injekt.get(),
                   private val shapeRenderer: ShapeRenderer = Injekt.get(),
                   private val gameCamera: GameCamera = Injekt.get(),
                   private val gameViewport: GameViewport = Injekt.get()) : EntitySystem() {
    private val mapRenderer = OrthogonalTiledMapRenderer(mapEntity.map.tiledMap, 1 / PPM, batch)
    private val b2DDebugRenderer = Box2DDebugRenderer()
    private val bodies = GdxArray<Body>()

    override fun update(deltaTime: Float) {
        stage.act()
        gameViewport.apply()
        stage.batch.projectionMatrix = gameCamera.combined

        drawImages()
        drawTiledMap()
//        drawPhysicsDebug()
    }

    private fun drawImages() {
        /**
         * Reposition the actors so they are drawn from the center (Box2D bodies' position is from center, not bottom-left)
         * It adds half the size of the actors and subtracts it when restoring.
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

        batch.color = ColorScheme.darkColor
        mapRenderer.setView(gameCamera)
        mapRenderer.render()
        batch.color = Color.WHITE
    }

    private fun drawPhysicsDebug() {
        fun drawXAtOrigins() {
            world.getBodies(bodies)
            shapeRenderer.projectionMatrix = gameCamera.combined
            shapeRenderer.color = Color.RED
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            for (i in 0 until bodies.size)
                if (bodies[i].type == BodyDef.BodyType.DynamicBody)
                    shapeRenderer.x(bodies[i].worldCenter, .05f)
            shapeRenderer.end()
            shapeRenderer.color = Color.WHITE
        }

        b2DDebugRenderer.render(world, gameCamera.combined)
        drawXAtOrigins()
    }
}