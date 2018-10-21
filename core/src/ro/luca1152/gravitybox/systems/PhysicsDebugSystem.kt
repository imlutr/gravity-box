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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import ktx.collections.GdxArray
import ro.luca1152.gravitybox.utils.GameCamera
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PhysicsDebugSystem(private val world: World,
                         private val camera: GameCamera = Injekt.get(),
                         private val shapeRenderer: ShapeRenderer = Injekt.get()) : EntitySystem() {
    private val renderer = Box2DDebugRenderer()
    private val bodies = GdxArray<Body>()

    override fun update(deltaTime: Float) {
        renderer.render(world, camera.combined)
        drawXAtOrigins()
    }

    private fun drawXAtOrigins() {
        world.getBodies(bodies)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.color = Color.RED
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        for (i in 0 until bodies.size)
            if (bodies[i].type == BodyDef.BodyType.DynamicBody)
                shapeRenderer.x(bodies[i].worldCenter, .05f)
        shapeRenderer.end()
        shapeRenderer.color = Color.WHITE
    }
}