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
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Contains a [ObjectMap] of [Entity]s. */
class LinkedEntityComponent : Component, Poolable {
    var entities = ObjectMap<String, Entity>()

    fun add(key: String, entity: Entity) {
        entities.put(key, entity)
    }

    fun get(key: String): Entity {
        require(entities.get(key) != null) { "No entity found for the given key (\"$key\")." }
        return entities.get(key)
    }

    fun remove(key: String): Entity {
        require(entities.get(key) != null) { "No entity found for the given key (\"$key\")." }
        return entities.remove(key)
    }

    override fun reset() {
        entities.clear()
    }

    companion object : ComponentResolver<LinkedEntityComponent>(LinkedEntityComponent::class.java)
}

val Entity.linkedEntity: LinkedEntityComponent
    get() = LinkedEntityComponent[this]

fun Entity.linkedEntity(
    context: Context,
    key: String,
    entity: Entity
) =
    add(createComponent<LinkedEntityComponent>(context).apply {
        add(key, entity)
    })!!

fun Entity.linkedEntity(context: Context) =
    add(createComponent<LinkedEntityComponent>(context))!!