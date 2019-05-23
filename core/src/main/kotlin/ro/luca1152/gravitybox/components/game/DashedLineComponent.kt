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

class DashedLineComponent : Component, Poolable {
    var startX = 0f
    var startY = 0f
    var endX = 0f
    var endY = 0f

    fun set(
        startX: Float, startY: Float,
        endX: Float, endY: Float
    ) {
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
    }

    override fun reset() {
        startX = 0f
        startY = 0f
        endX = 0f
        endY = 0f
    }

    companion object : ComponentResolver<DashedLineComponent>(DashedLineComponent::class.java)
}

val Entity.dashedLine: DashedLineComponent
    get() = DashedLineComponent[this]

fun Entity.dashedLine(
    context: Context,
    startX: Float, startY: Float,
    endX: Float, endY: Float
) = add(createComponent<DashedLineComponent>(context).apply {
    set(startX, startY, endX, endY)
})!!