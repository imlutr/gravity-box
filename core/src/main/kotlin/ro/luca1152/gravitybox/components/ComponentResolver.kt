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

@file:Suppress("PrivatePropertyName", "HasPlatformType")

package ro.luca1152.gravitybox.components


import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity

/** Components with a companion object which extends this class can be referenced in an OOP  style.*/
open class ComponentResolver<T : Component>(componentClass: Class<T>) {
    private val MAPPER = ComponentMapper.getFor(componentClass)!!
    operator fun get(entity: Entity) = MAPPER[entity]
}