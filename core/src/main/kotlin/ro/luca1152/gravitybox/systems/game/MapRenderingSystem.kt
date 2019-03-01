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
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import ro.luca1152.gravitybox.PPM
import ro.luca1152.gravitybox.components.map
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.GameViewport
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MapRenderingSystem(
    private val mapEntity: Entity,
    private val batch: Batch = Injekt.get(),
    private val gameViewport: GameViewport = Injekt.get(),
    private val gameCamera: GameCamera = Injekt.get()
) : EntitySystem() {
    private val mapRenderer = OrthogonalTiledMapRenderer(mapEntity.map.tiledMap, 1 / PPM, batch)

    override fun update(deltaTime: Float) {
        // Update the map in case the level changed
        mapRenderer.map = mapEntity.map.tiledMap

        gameViewport.apply()
        batch.projectionMatrix = gameCamera.combined
        drawMap()
    }

    private fun drawMap() {
        batch.color = ColorScheme.currentDarkColor
        mapRenderer.setView(gameCamera)
        mapRenderer.render()
        batch.color = Color.WHITE
    }
}