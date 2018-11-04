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
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import ro.luca1152.gravitybox.pixelsToMeters

class MapObjectComponent : Component, Pool.Poolable {
    var obj: MapObject = MapObject()
    val position = Vector2()
    val worldCenter = Vector2()
    var width = 0f
    var height = 0f

    fun set(mapObject: MapObject) {
        this.obj = mapObject
        width = (obj.properties["width"] as Float).pixelsToMeters
        height = (obj.properties["height"] as Float).pixelsToMeters
        position.set((obj.properties["x"] as Float).pixelsToMeters, (obj.properties["y"] as Float).pixelsToMeters)
        worldCenter.set(position.x + width / 2f, position.y + height / 2f)
    }

    /** Resets the component for reuse. */
    override fun reset() {
        position.set(0f, 0f)
        worldCenter.set(0f, 0f)
        width = 0f; height = 0f
    }

    companion object : ComponentResolver<MapObjectComponent>(MapObjectComponent::class.java)
}

val Entity.mapObject: MapObjectComponent
    get() = MapObjectComponent[this]