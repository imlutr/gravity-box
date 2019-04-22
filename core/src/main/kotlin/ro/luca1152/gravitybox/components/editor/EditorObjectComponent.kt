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
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Contains information about level editor objects. */
class EditorObjectComponent : Component, Poolable {
    var isDeleted = false
    var isRotating = false
    var isSelected = false

    var isDraggingHorizontally = false
    var isDraggingVertically = false
    val isDragging
        get() = isDraggingHorizontally || isDraggingVertically

    var isResizingLeftwards = false
    var isResizingRightwards = false
    var isResizingDownwards = false
    var isResizingUpwards = false
    val isResizing
        get() = isResizingLeftwards || isResizingRightwards || isResizingDownwards || isResizingUpwards

    fun resetResizingBooleans() {
        isResizingLeftwards = false
        isResizingRightwards = false
        isResizingDownwards = false
        isResizingUpwards = false
    }

    override fun reset() {
        isSelected = false
        isDeleted = false
        isRotating = false
        isDraggingHorizontally = false
        isDraggingVertically = false
        resetResizingBooleans()
    }

    companion object : ComponentResolver<EditorObjectComponent>(EditorObjectComponent::class.java)
}

val Entity.editorObject: EditorObjectComponent
    get() = EditorObjectComponent[this]

fun Entity.editorObject(context: Context) =
    add(createComponent<EditorObjectComponent>(context))!!