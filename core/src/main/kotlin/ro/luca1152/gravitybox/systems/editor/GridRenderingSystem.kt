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

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.graphics.copy
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Suppress("PrivatePropertyName")
class GridRenderingSystem(private val gameStage: GameStage = Injekt.get(),
                          private val manager: AssetManager = Injekt.get()) : EntitySystem() {
    private val LINE_COLOR = ColorScheme.currentDarkColor.copy(alpha = .2f)
    private val LINE_WIDTH = 2f.pixelsToMeters
    private lateinit var grid: Group

    override fun addedToEngine(engine: Engine?) {
        grid = Group().apply {
            gameStage.addActor(this)
            addActor(createVerticalLines())
            addActor(createHorizontalLines())
        }
    }

    private fun createVerticalLines() = Group().apply {
        for (x in 0 until 50) {
            addActor(Image(manager.get<Texture>("graphics/pixel.png")).apply {
                color = LINE_COLOR
                setSize(LINE_WIDTH, 50f)
                setPosition(x.toFloat(), 0f)
            })
        }
    }

    private fun createHorizontalLines() = Group().apply {
        for (y in 0 until 50) {
            addActor(Image(manager.get<Texture>("graphics/pixel.png")).apply {
                color = LINE_COLOR
                setSize(50f, LINE_WIDTH)
                setPosition(0f, y.toFloat())
            })
        }
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        grid.toBack()
    }

    override fun removedFromEngine(engine: Engine?) {
        grid.remove()
    }
}