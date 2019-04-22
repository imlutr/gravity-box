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

/** Contains a color type (light/dark/darker). */
class ColorComponent : Component, Poolable {
    var colorType = ColorType.NULL

    fun set(colorType: ColorType) {
        this.colorType = colorType
    }

    override fun reset() {
        colorType = ColorType.NULL
    }

    companion object : ComponentResolver<ColorComponent>(ColorComponent::class.java)
}

enum class ColorType {
    LIGHT,
    DARK,
    DARKER_DARK,
    NULL

}

val Entity.color: ColorComponent
    get() = ColorComponent[this]

fun Entity.color(context: Context, colorType: ColorType) =
    add(createComponent<ColorComponent>(context).apply {
        set(colorType)
    })!!
