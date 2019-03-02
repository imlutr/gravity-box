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
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
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

class MoveCommand(
    override val affectedEntity: Entity,
    private val deltaX: Float, private val deltaY: Float
) : Command() {
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

class RotateCommand(
    override val affectedEntity: Entity,
    private val deltaAngle: Float
) : Command() {
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

class AddCommand(
    override val affectedEntity: Entity,
    private val engine: PooledEngine = Injekt.get()
) : Command() {
    override fun execute() {
        affectedEntity.tryGet(ImageComponent)?.run {
            img.isVisible = true
            img.touchable = Touchable.enabled

            affectedEntity.tryGet(BodyComponent)?.run {
                body = imageToBox2DBody(bodyType, categoryBits, maskBits, density, friction)
            }
        }
        affectedEntity.tryGet(TouchableBoundsComponent)?.run {
            boundsImage.touchable = Touchable.enabled
        }
        affectedEntity.tryGet(ColorComponent)?.run {
            colorType = ColorType.DARK
        }
        affectedEntity.tryGet(MapObjectComponent).run {
            val newId = affectedEntity.mapObject.id
            engine.getEntitiesFor(Family.all(MapObjectComponent::class.java).exclude(DeletedMapObjectComponent::class.java).get())
                .forEach {
                    if (it != affectedEntity && it.mapObject.id >= newId)
                        it.mapObject.id++
                }
        }
        affectedEntity.remove(DeletedMapObjectComponent::class.java)
    }

    override fun unexecute() {
        affectedEntity.tryGet(ImageComponent)?.run {
            img.isVisible = false
            img.touchable = Touchable.disabled
        }
        affectedEntity.tryGet(TouchableBoundsComponent)?.run {
            boundsImage.touchable = Touchable.disabled
        }
        affectedEntity.tryGet(BodyComponent)?.run {
            destroyBody()
        }
        affectedEntity.tryGet(MapObjectComponent).run {
            val deletedId = affectedEntity.mapObject.id
            engine.getEntitiesFor(Family.all(MapObjectComponent::class.java).exclude(DeletedMapObjectComponent::class.java).get())
                .forEach {
                    if (it.mapObject.id > deletedId)
                        it.mapObject.id--
                }
        }
        affectedEntity.add(engine.createComponent(DeletedMapObjectComponent::class.java))
        affectedEntity.remove(SelectedObjectComponent::class.java)
    }
}

class DeleteCommand(override val affectedEntity: Entity) : Command() {
    private val addCommand = AddCommand(affectedEntity)

    override fun execute() {
        addCommand.unexecute()
    }

    override fun unexecute() {
        addCommand.execute()
    }
}

/**
 * Resizes the [affectedEntity]. If the entity was also moved after it was resized, values should be
 * given to [deltaX] and [deltaY]. This is not done in an additional [MoveCommand] because undo() would
 * then have to be called two times (or the undo button pressed two times), instead of once.
 */
class ResizeCommand(
    override val affectedEntity: Entity,
    private val deltaWidth: Float, private val deltaHeight: Float,
    private val deltaX: Float = 0f, private val deltaY: Float = 0f
) : Command() {
    init {
        check(affectedEntity.tryGet(ImageComponent) != null)
        { "The [affectedEntity] must have an [ImageComponent]." }
    }

    override fun execute() {
        affectedEntity.image.run {
            width += deltaWidth
            height += deltaHeight
            centerX += deltaX
            centerY += deltaY
        }
    }

    override fun unexecute() {
        affectedEntity.image.run {
            width -= deltaWidth
            height -= deltaHeight
            centerX -= deltaX
            centerY -= deltaY
        }
    }
}