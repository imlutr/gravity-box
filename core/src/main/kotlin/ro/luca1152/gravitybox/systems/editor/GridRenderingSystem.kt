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
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.graphics.copy
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.pixelsToMeters
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.ui.Colors

@Suppress("PrivatePropertyName")
/** Creates and renders an infinite grid. */
class GridRenderingSystem(context: Context) : EntitySystem() {
    companion object {
        private const val GRID_START_POSITION = -500
        private const val GRID_END_POSITION = 500
        private const val GRID_LENGTH = GRID_END_POSITION - GRID_START_POSITION
        private val LINE_THICKNESS = 2f.pixelsToMeters
        private val LINE_COLOR = Colors.gameColor.copy(alpha = .2f)
    }

    private val gameStage: GameStage = context.inject()
    private val manager: AssetManager = context.inject()

    private val gridGroup = Group()
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        gridGroup.run {
            clear()
            addActor(createVerticalLines())
            addActor(createHorizontalLines())
        }
        gameStage.addActor(gridGroup)
    }

    private fun createVerticalLines() = Group().apply {
        for (x in GRID_START_POSITION..GRID_END_POSITION) {
            addActor(Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
                color = LINE_COLOR
                setSize(LINE_THICKNESS, GRID_LENGTH.toFloat())
                setPosition(x.toFloat(), GRID_START_POSITION.toFloat())
            })
        }
    }

    private fun createHorizontalLines() = Group().apply {
        for (y in GRID_START_POSITION..GRID_END_POSITION) {
            addActor(Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
                color = LINE_COLOR
                setSize(GRID_LENGTH.toFloat(), LINE_THICKNESS)
                setPosition(GRID_START_POSITION.toFloat(), y.toFloat())
            })
        }
    }

    override fun update(deltaTime: Float) {
        gridGroup.toBack()
    }

    override fun removedFromEngine(engine: Engine?) {
        gridGroup.remove()
    }
}