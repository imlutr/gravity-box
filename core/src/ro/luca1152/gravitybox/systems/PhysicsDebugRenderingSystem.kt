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

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.components.utils.tryGet
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.GameCamera
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Suppress("LibGDXFlushInsideLoop")
class PhysicsDebugRenderingSystem(private val world: World = Injekt.get(),
                                  private val batch: Batch = Injekt.get(),
                                  private val shapeRenderer: ShapeRenderer = Injekt.get(),
                                  private val gameViewport: GameViewport = Injekt.get(),
                                  private val gameCamera: GameCamera = Injekt.get()) : EntitySystem() {
    private val b2DDebugRenderer = Box2DDebugRenderer()

    override fun addedToEngine(engine: Engine?) {
        shapeRenderer.setAutoShapeType(true)
        Gdx.gl20.glLineWidth(10f)
    }

    override fun update(deltaTime: Float) {
        gameViewport.apply()
        batch.projectionMatrix = gameCamera.combined
        shapeRenderer.projectionMatrix = gameCamera.combined
        drawDebug()
    }

    private fun drawDebug() {
        b2DDebugRenderer.render(world, gameCamera.combined)
        shapeRenderer.begin()
        drawXAtOrigins()
        drawPlatforms()
        shapeRenderer.end()
    }

    private fun drawXAtOrigins() {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line)
        for (entity in engine.getEntitiesFor(Family.all(PhysicsComponent::class.java).get())) {
            val body = entity.physics.body
            if (body.type == BodyDef.BodyType.DynamicBody && body.userData != null) {
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

    /**
     * While Box2DDebugRenderer could draw this automatically, this way dynamic platforms
     * are drawn hollowly, while the static ones are drawn fully.
     */
    private fun drawPlatforms() {
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
}