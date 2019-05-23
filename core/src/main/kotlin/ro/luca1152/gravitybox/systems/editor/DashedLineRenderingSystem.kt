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

package ro.luca1152.gravitybox.systems.editor

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pools
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import ro.luca1152.gravitybox.utils.ui.Colors

class DashedLineRenderingSystem(context: Context) : IteratingSystem(Family.all(DashedLineComponent::class.java).get()) {
    private val shapeRenderer: ShapeRenderer = context.inject()
    private val batch: Batch = context.inject()
    private val gameCamera: GameCamera = context.inject()

    companion object {
        const val COLOR_ALPHA = .4f
        val DASH_WIDTH = 10f.pixelsToMeters
        val DASH_THICKNESS = 3f.pixelsToMeters
        val DASH_GAP = 7f.pixelsToMeters
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        updateLineEnds(entity)
        drawDashedLine(entity)
    }

    private fun updateLineEnds(entity: Entity) {
        if (entity.tryGet(LinkedEntityComponent) == null) return

        val platform = entity.linkedEntity.get("platform")
        val mockPlatform = entity.linkedEntity.get("mockPlatform")
        entity.dashedLine.run {
            startX = platform.scene2D.centerX
            startY = platform.scene2D.centerY
            endX = mockPlatform.scene2D.centerX
            endY = mockPlatform.scene2D.centerY
        }
    }

    private fun drawDashedLine(entity: Entity) {
        entity.dashedLine.run {
            val direction = Pools.obtain(Vector2::class.java).set(endX - startX, endY - startY)
            val lineLength = direction.len()
            direction.nor()
            shapeRenderer.run {
                projectionMatrix = gameCamera.combined
                setColor(Colors.gameColor.r, Colors.gameColor.g, Colors.gameColor.b, COLOR_ALPHA * batch.color.a)
                Gdx.gl.glEnable(GL20.GL_BLEND)
                begin(ShapeRenderer.ShapeType.Filled)

                var currentLength = 0f
                while (true) {
                    val currentX = startX + direction.x * currentLength
                    val currentY = startY + direction.y * currentLength
                    val dashWidth =
                        if (currentLength + DASH_WIDTH > lineLength) Math.abs(DASH_WIDTH - Math.abs(lineLength - currentLength + DASH_WIDTH))
                        else DASH_WIDTH
                    rectLine(
                        currentX, currentY,
                        currentX + direction.x * dashWidth, currentY + direction.y * dashWidth,
                        DASH_THICKNESS
                    )
                    currentLength += DASH_WIDTH + DASH_GAP
                    if (currentLength > lineLength) {
                        break
                    }
                }

                end()
                Gdx.gl.glDisable(GL20.GL_BLEND)
                Pools.free(direction)
            }
        }
    }
}