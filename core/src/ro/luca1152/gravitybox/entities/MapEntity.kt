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

package ro.luca1152.gravitybox.entities

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.World
import ro.luca1152.gravitybox.PPM
import ro.luca1152.gravitybox.components.MapComponent
import ro.luca1152.gravitybox.components.map
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.MapBodyBuilder
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MapEntity(levelNumber: Int,
                world: World = Injekt.get()) : Entity() {
    companion object {
        const val GRAVITY = -25f
    }

    init {
        add(MapComponent(levelNumber))

        // Update the colors
        ColorScheme.lightColor = ColorScheme.getLightColor(map.hue)
        ColorScheme.darkColor = ColorScheme.getDarkColor(map.hue)
        ColorScheme.lightColor2 = ColorScheme.getLightColor2(map.hue)
        ColorScheme.darkColor2 = ColorScheme.getDarkColor2(map.hue)

        // Put the collision boxes on the tiles
        MapBodyBuilder.buildShapes(map.tiledMap, PPM, world)
    }
}