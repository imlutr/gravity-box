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

package ro.luca1152.gravitybox.components.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.engine

/** Indicates that the entity is an object from a game map. */
class MapObjectComponent : Component, Poolable {
    var id = -1

    fun set(id: Int) {
        this.id = id
    }

    override fun reset() {
        id = -1
    }

    enum class MapObjectType {
        PLATFORM
    }

    companion object : ComponentResolver<MapObjectComponent>(MapObjectComponent::class.java)
}

val Entity.mapObject: MapObjectComponent
    get() = MapObjectComponent[this]

fun Entity.mapObject(id: Int) =
    add(engine.createComponent(MapObjectComponent::class.java).apply {
        set(id)
    })!!