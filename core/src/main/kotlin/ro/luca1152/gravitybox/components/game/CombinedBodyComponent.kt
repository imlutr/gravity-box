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
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Indicates that the Entity's body was combined (welded) with another body. */
class CombinedBodyComponent : Component, Poolable {
    var newBodyEntity: Entity? = null
    var isCombinedHorizontally = false
    var isCombinedVertically = false
    var entityContainsBody = false

    fun set(
        newBodyEntity: Entity,
        isCombinedHorizontally: Boolean = false,
        isCombinedVertically: Boolean = false,
        entityContainsBody: Boolean = false
    ) {
        this.newBodyEntity = newBodyEntity
        this.isCombinedHorizontally = isCombinedHorizontally
        this.isCombinedVertically = isCombinedVertically
        this.entityContainsBody = entityContainsBody
    }

    override fun reset() {
        newBodyEntity = null
        isCombinedHorizontally = false
        isCombinedVertically = false
        entityContainsBody = false
    }

    companion object : ComponentResolver<CombinedBodyComponent>(CombinedBodyComponent::class.java)
}

val Entity.combinedBody: CombinedBodyComponent
    get() = CombinedBodyComponent[this]

fun Entity.combinedBody(
    context: Context,
    newBodyEntity: Entity,
    isCombinedHorizontally: Boolean = false,
    isCombinedVertically: Boolean = false,
    entityContainsBody: Boolean = false
) = add(createComponent<CombinedBodyComponent>(context).apply {
    set(newBodyEntity, isCombinedHorizontally, isCombinedVertically, entityContainsBody)
})!!
