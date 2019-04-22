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
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.DebugShapeComponent
import ro.luca1152.gravitybox.components.editor.debugShape
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.GameViewport

/** Renders debug shapes. */
class DebugRenderingSystem(context: Context) : IteratingSystem(Family.all(DebugShapeComponent::class.java).get()) {
    private val shapeRenderer: ShapeRenderer = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val gameCamera: GameCamera = context.inject()

    init {
        shapeRenderer.setAutoShapeType(true)
        Gdx.gl20.glLineWidth(3f)
    }

    override fun update(deltaTime: Float) {
        gameViewport.apply()
        shapeRenderer.run {
            projectionMatrix = gameCamera.combined
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
            entity.debugShape.polygon != null -> drawPolygon(entity.debugShape.polygon as Polygon)
            entity.debugShape.rectangle != null -> drawRectangle(entity.debugShape.rectangle as Rectangle)
            entity.debugShape.point != null -> drawPoint(entity.debugShape.point as Vector2)
        }
    }

    private fun drawPolygon(polygon: Polygon) {
        shapeRenderer.color = Color.RED
        shapeRenderer.set(ShapeRenderer.ShapeType.Line)
        shapeRenderer.polygon(polygon.transformedVertices)
    }

    private fun drawRectangle(rectangle: Rectangle) {
        shapeRenderer.color = Color.RED
        shapeRenderer.set(ShapeRenderer.ShapeType.Line)
        shapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
    }

    private fun drawPoint(point: Vector2) {
        shapeRenderer.color = Color.GREEN
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.circle(point.x, point.y, .07f, 16)
    }
}