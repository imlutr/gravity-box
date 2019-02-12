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

package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.assets.getAsset
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import ro.luca1152.gravitybox.entities.EntityFactory
import ro.luca1152.gravitybox.utils.box2d.MapBodyBuilder
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Contains a [TiledMap]. */
class MapComponent(private val manager: AssetManager = Injekt.get()) : Component, Poolable {
    var tiledMap = TiledMap()
    var levelNumber = 0
    var widthInTiles = 0
    var heightInTiles = 0
    var hue = 180
    var totalPointsNumber = 0
    var collectedPoints = 0
    val isFinished
        get() = collectedPoints == totalPointsNumber

    fun set(levelNumber: Int) {
        this.levelNumber = levelNumber

        // Update the map
        tiledMap = manager.getAsset("maps/map-$levelNumber.tmx")

        // Build the platforms & points
        EntityFactory.createPlatforms(MapBodyBuilder.buildPlatforms(tiledMap))
        EntityFactory.createPoints(MapBodyBuilder.buildPoints(this))

        // Update the map properties
        widthInTiles = tiledMap.properties.get("widthInTiles") as Int
        heightInTiles = tiledMap.properties.get("heightInTiles") as Int
        hue = tiledMap.properties.get("hue") as Int

        // The new map may have a different hue so the color scheme must be updated
//        ColorScheme.hue = hue.toFloat()
        ColorScheme.updateColors()
    }

    /** Resets the component for reuse. */
    override fun reset() {
        levelNumber = 0
        widthInTiles = 0; heightInTiles = 0; hue = 180
        totalPointsNumber = 0; collectedPoints = 0
    }

    companion object : ComponentResolver<MapComponent>(MapComponent::class.java) {
        const val GRAVITY = -25f
    }
}

val Entity.map: MapComponent
    get() = MapComponent[this]
