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

package ro.luca1152.gravitybox.components.editor

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.game.PlatformComponent
import ro.luca1152.gravitybox.utils.kotlin.Reference
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.ui.button.ToggleButton

/** Keeps track of UI events. */
class InputComponent : Component, Poolable {
    var toggledButton = Reference<ToggleButton>()
    var placeToolObjectType: Class<out Any> = PlatformComponent::class.java
    var isPanning = false
    var isZooming = false

    fun set(toggledButton: Reference<ToggleButton>) {
        this.toggledButton = toggledButton
    }

    override fun reset() {
        toggledButton = Reference()
        placeToolObjectType = PlatformComponent::class.java
        isPanning = false
        isZooming = false
    }

    companion object : ComponentResolver<InputComponent>(InputComponent::class.java)
}

val Entity.input: InputComponent
    get() = InputComponent[this]

fun Entity.input(context: Context, toggledButton: Reference<ToggleButton>) =
    add(createComponent<InputComponent>(context).apply {
        set(toggledButton)
    })!!