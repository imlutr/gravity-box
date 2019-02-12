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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import ro.luca1152.gravitybox.components.DebugComponent
import ro.luca1152.gravitybox.components.debug
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class DebugRenderingSystem(private val shapeRenderer: ShapeRenderer = Injekt.get(),
                           private val gameViewport: GameViewport = Injekt.get(),
                           private val gameCamera: GameCamera = Injekt.get()) : IteratingSystem(Family.all(DebugComponent::class.java).get()) {
    init {
        shapeRenderer.setAutoShapeType(true)
        Gdx.gl20.glLineWidth(10f)
    }

    override fun update(deltaTime: Float) {
        gameViewport.apply()
        shapeRenderer.run {
            projectionMatrix = gameCamera.combined
            color = Color.RED
            begin()
        }
        super.update(deltaTime)
        shapeRenderer.run {
            end()
            color = Color.WHITE
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        when {
            entity.debug.polygon != null -> drawPolygon(entity.debug.polygon as Polygon)
            entity.debug.rectangle != null -> drawRectangle(entity.debug.rectangle as Rectangle)
            entity.debug.point != null -> drawPoint(entity.debug.point as Vector2)
        }
    }

    private fun drawPolygon(polygon: Polygon) {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line)
        shapeRenderer.polygon(polygon.transformedVertices)
    }

    private fun drawRectangle(rectangle: Rectangle) {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line)
        shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
    }

    private fun drawPoint(point: Vector2) {
        shapeRenderer.set(ShapeRenderer.ShapeType.Point)
        shapeRenderer.point(point.x, point.y, 0f)
    }
}