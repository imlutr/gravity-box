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
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import ro.luca1152.gravitybox.components.utils.tryGet
import java.util.*

class UndoRedoComponent : Component, Poolable {
    val commandsToUndo = Stack<Command>()
    val commandsToRedo = Stack<Command>()
    var levelsToUndo = 0
    var levelsToRedo = 0

    fun canUndo() = commandsToUndo.size >= levelsToUndo + 1

    fun undo() {
        if (!canUndo())
            return
        levelsToUndo++
    }

    fun canRedo() = commandsToRedo.size >= levelsToRedo + 1

    fun redo() {
        if (!canRedo())
            return
        levelsToRedo++
    }

    fun addExecutedCommand(command: Command) {
        commandsToUndo.add(command)
        commandsToRedo.removeAllElements()
    }

    override fun reset() {
        commandsToUndo.removeAllElements()
        commandsToRedo.removeAllElements()
        levelsToUndo = 0
        levelsToRedo = 0
    }

    companion object : ComponentResolver<UndoRedoComponent>(UndoRedoComponent::class.java)
}

val Entity.undoRedo: UndoRedoComponent
    get() = UndoRedoComponent[this]

abstract class Command {
    abstract val affectedEntity: Entity
    abstract fun execute()
    abstract fun unexecute()
}

class MoveCommand(override val affectedEntity: Entity,
                  private val deltaX: Float, private val deltaY: Float) : Command() {
    init {
        check(affectedEntity.tryGet(ImageComponent) != null)
        { "The [affectedEntity] must have an [ImageComponent]." }
    }

    override fun execute() {
        affectedEntity.image.img.moveBy(deltaX, deltaY)
    }

    override fun unexecute() {
        affectedEntity.image.img.moveBy(-deltaX, -deltaY)
    }
}

class RotateCommand(override val affectedEntity: Entity,
                    private val deltaAngle: Float) : Command() {
    init {
        check(affectedEntity.tryGet(ImageComponent) != null)
        { "The [affectedEntity] must have an [ImageComponent]." }
    }

    override fun execute() {
        affectedEntity.image.img.rotation += deltaAngle
    }

    override fun unexecute() {
        affectedEntity.image.img.rotation -= deltaAngle
    }
}

class DeleteCommand(override val affectedEntity: Entity) : Command() {
    override fun execute() {
        affectedEntity.tryGet(ImageComponent)?.run {
            img.isVisible = false
            img.touchable = Touchable.disabled
        }
        affectedEntity.tryGet(TouchableBoundsComponent)?.run {
            boundsImage.touchable = Touchable.disabled
        }
        affectedEntity.remove(SelectedObjectComponent::class.java)
    }

    override fun unexecute() {
        affectedEntity.tryGet(ImageComponent)?.run {
            img.isVisible = true
            img.touchable = Touchable.enabled
        }
        affectedEntity.tryGet(TouchableBoundsComponent)?.run {
            boundsImage.touchable = Touchable.enabled
        }
        affectedEntity.tryGet(ColorComponent)?.run {
            colorType = ColorType.DARK
        }
    }
}

