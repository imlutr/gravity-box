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
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.MapBodyBuilder
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Contains the data of a TiledMap, such as its width, height, hue.
 */
class MapComponent(private val manager: AssetManager = Injekt.get()) : Component, Poolable {
    var tiledMap = TiledMap() // Initialized with an empty TiledMap to avoid nullable type

    // Properties
    var levelNumber = 0
    /** The width of the map, in tiles. */
    var width = 0
    /** The height of the map, in tiles. */
    var height = 0
    /** The hue of the color of the map, in [0, 360] range. */
    var hue = 180 // The initial hue is 180
    /** How many points a map has. */
    var totalPointsNumber = 0
    /** The number of points the player collected. */
    var collectedPoints = 0
    /** True if the player collected every point. */
    val isFinished
        get() = collectedPoints == totalPointsNumber

    /** Initializes the component. */
    fun set(levelNumber: Int) {
        this.levelNumber = levelNumber

        // Update the map
        tiledMap = manager.getAsset("maps/map-$levelNumber.tmx")

        // Build the platforms & points
        EntityFactory.createPlatforms(MapBodyBuilder.buildPlatforms(tiledMap))
        EntityFactory.createPoints(MapBodyBuilder.buildPoints(this))

        // Update the map properties
        width = tiledMap.properties.get("width") as Int
        height = tiledMap.properties.get("height") as Int
        hue = tiledMap.properties.get("hue") as Int

        // The new map may have a different hue so the color scheme must be updated
        ColorScheme.updateColors(hue)
    }

    /** Resets the component for reuse. */
    override fun reset() {
        levelNumber = 0
        width = 0; height = 0; hue = 180
        totalPointsNumber = 0; collectedPoints = 0
    }

    companion object : ComponentResolver<MapComponent>(MapComponent::class.java) {
        const val GRAVITY = -25f
    }
}

val Entity.map: MapComponent
    get() = MapComponent[this]
