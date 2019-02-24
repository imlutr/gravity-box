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
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.graphics.copy
import ro.luca1152.gravitybox.components.LevelComponent
import ro.luca1152.gravitybox.components.newMap
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

@Suppress("PrivatePropertyName")
class GridRenderingSystem(private val gameStage: GameStage = Injekt.get(),
                          private val manager: AssetManager = Injekt.get()) : EntitySystem() {
    private val LINE_COLOR = ColorScheme.currentDarkColor.copy(alpha = .2f)
    private val LINE_WIDTH = 2f.pixelsToMeters
    private val gridGroup = Group()
    private lateinit var levelEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get())
        gridGroup.run {
            clear()
            addActor(createVerticalLines(levelEntity.newMap.widthInTiles, levelEntity.newMap.heightInTiles))
            addActor(createHorizontalLines(levelEntity.newMap.widthInTiles, levelEntity.newMap.heightInTiles))
        }
        gameStage.addActor(gridGroup)
    }

    private fun createVerticalLines(mapWidth: Int, mapHeight: Int) = Group().apply {
        for (x in 0 until mapWidth + 1) {
            addActor(Image(manager.get<Texture>("graphics/pixel.png")).apply {
                color = LINE_COLOR
                setSize(LINE_WIDTH, mapHeight.toFloat())
                setPosition(x.toFloat(), 0f)
            })
        }
    }

    private fun createHorizontalLines(mapWidth: Int, mapHeight: Int) = Group().apply {
        for (y in 0 until mapHeight + 1) {
            addActor(Image(manager.get<Texture>("graphics/pixel.png")).apply {
                color = LINE_COLOR
                setSize(mapWidth.toFloat(), LINE_WIDTH)
                setPosition(0f, y.toFloat())
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