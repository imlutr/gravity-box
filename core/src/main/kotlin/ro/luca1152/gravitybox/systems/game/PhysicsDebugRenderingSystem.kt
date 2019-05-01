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

package ro.luca1152.gravitybox.systems.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.GameViewport
import ro.luca1152.gravitybox.utils.kotlin.tryGet

@Suppress("LibGDXFlushInsideLoop", "unused")
/** Renders Box2D debug shapes. */
class PhysicsDebugRenderingSystem(context: Context) : EntitySystem() {
    private val world: World = context.inject()
    private val shapeRenderer: ShapeRenderer = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val gameCamera: GameCamera = context.inject()
    private val b2DDebugRenderer = Box2DDebugRenderer()

    override fun addedToEngine(engine: Engine?) {
        shapeRenderer.setAutoShapeType(true)
        Gdx.gl20.glLineWidth(3f)
    }

    override fun update(deltaTime: Float) {
        gameViewport.apply()
        shapeRenderer.projectionMatrix = gameCamera.combined
        drawDebugPhysics()
    }

    private fun drawDebugPhysics() {
        b2DDebugRenderer.render(world, gameCamera.combined)
        shapeRenderer.begin()
        drawXAtOrigins()
        shapeRenderer.end()
    }

    private fun drawXAtOrigins() {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line)
        for (entity in engine.getEntitiesFor(Family.all(BodyComponent::class.java).get())) {
            if (entity.body.body == null) return
            val body = entity.body.body!!
            if (body.type == BodyDef.BodyType.DynamicBody && body.userData != null) {
                (body.userData as Entity).run {
                    when {
                        this.tryGet(FinishComponent) != null -> shapeRenderer.color = Color.LIME
                        this.tryGet(PlayerComponent) != null -> shapeRenderer.color = Color.RED
                        this.tryGet(BulletComponent) != null -> shapeRenderer.color = Color.YELLOW
                    }
                    if (this.tryGet(PlayerComponent) != null)
                        shapeRenderer.circle(scene2D.centerX, scene2D.centerY, .04f)
                }

            }
        }
    }
}