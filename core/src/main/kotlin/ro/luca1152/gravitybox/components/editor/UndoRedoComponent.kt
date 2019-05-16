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
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.editor.MovingMockPlatformEntity
import ro.luca1152.gravitybox.entities.game.DashedLineEntity
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.UpdateRoundedPlatformsEvent
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.kotlin.removeComponent
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import java.util.*

/** Contains undo and redo commands. */
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

fun Entity.undoRedo(context: Context) =
    add(createComponent<UndoRedoComponent>(context))!!

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
        check(affectedEntity.tryGet(Scene2DComponent) != null)
        { "The affectedEntity must have a Scene2DComponent in order to be moved." }
    }

    override fun execute() {
        affectedEntity.scene2D.group.moveBy(deltaX, deltaY)
    }

    override fun unexecute() {
        affectedEntity.scene2D.group.moveBy(-deltaX, -deltaY)
    }
}

class RotateCommand(
    override val affectedEntity: Entity,
    private val deltaAngle: Float
) : Command() {
    init {
        check(affectedEntity.tryGet(Scene2DComponent) != null)
        { "The affectedEntity must have a Scene2DComponent in order to be rotated." }
    }

    override fun execute() {
        affectedEntity.scene2D.rotation += deltaAngle
        if (affectedEntity.tryGet(MovingObjectComponent) != null) {
            affectedEntity.linkedEntity.get("mockPlatform").scene2D.rotation += deltaAngle
        }
    }

    override fun unexecute() {
        affectedEntity.scene2D.rotation -= deltaAngle
        if (affectedEntity.tryGet(MovingObjectComponent) != null) {
            affectedEntity.linkedEntity.get("mockPlatform").scene2D.rotation -= deltaAngle
        }
    }
}

class AddCommand(
    private val context: Context,
    override var affectedEntity: Entity,
    private val mapEntity: Entity
) : Command() {
    // Injected objects
    private val eventQueue: EventQueue = context.inject()

    /**
     * True if the [affectedEntity] is a mock object.
     * I keep this in a variable as the mock platform entity will be deleted in [unexecute()].
     */
    private val isMockObject = affectedEntity.tryGet(MockMapObjectComponent) != null

    /** Holds the linked platform of a mock platform. I do this because the mock platform entity is deleted in [unexecute()]. */
    private val platform = if (affectedEntity.tryGet(MockMapObjectComponent) != null)
        affectedEntity.linkedEntity.get("platform") else null

    override fun execute() {
        if (isMockObject) {
            MakeObjectMovingCommand(context, platform!!).execute()
            affectedEntity = platform.linkedEntity.get("mockPlatform")
        }
        affectedEntity.tryGet(EditorObjectComponent)?.run {
            isDeleted = false
        }
        affectedEntity.tryGet(Scene2DComponent)?.run {
            isVisible = true
            isTouchable = true

            affectedEntity.tryGet(BodyComponent)?.run {
                body = if (affectedEntity.tryGet(PlayerComponent) != null) {
                    affectedEntity.scene2D.toBody(context, bodyType, categoryBits, maskBits, density, friction, 0.02f)
                } else {
                    affectedEntity.scene2D.toBody(context, bodyType, categoryBits, maskBits, density, friction)
                }
            }
        }
        if (affectedEntity.tryGet(MovingObjectComponent) != null) {
            affectedEntity.linkedEntity.get("mockPlatform").run {
                scene2D.run {
                    isVisible = true
                    isTouchable = true
                }
                editorObject.isDeleted = false
            }
        }
        affectedEntity.tryGet(ExtendedTouchComponent)?.run {
            boundsImage.touchable = Touchable.enabled
        }
        affectedEntity.tryGet(ColorComponent)?.run {
            colorType = ColorType.DARK
        }
        affectedEntity.tryGet(CollectiblePointComponent)?.run {
            mapEntity.map.pointsCount++
        }
        eventQueue.add(UpdateRoundedPlatformsEvent())
    }

    override fun unexecute() {
        affectedEntity.editorObject.run {
            isSelected = false
            isDeleted = true
        }
        affectedEntity.tryGet(Scene2DComponent)?.run {
            isVisible = false
            isTouchable = false
        }
        if (affectedEntity.tryGet(MovingObjectComponent) != null) {
            affectedEntity.linkedEntity.get("mockPlatform").run {
                scene2D.run {
                    isVisible = false
                    isTouchable = false
                }
                editorObject.isDeleted = true
            }
        }
        affectedEntity.tryGet(ExtendedTouchComponent)?.run {
            boundsImage.touchable = Touchable.disabled
        }
        affectedEntity.tryGet(BodyComponent)?.run {
            destroyBody()
        }
        affectedEntity.tryGet(MockMapObjectComponent)?.run {
            MakeObjectNonMovingCommand(context, affectedEntity.linkedEntity.get("platform")).execute()
        }
        affectedEntity.tryGet(CollectiblePointComponent)?.run {
            mapEntity.map.pointsCount--
        }
        eventQueue.add(UpdateRoundedPlatformsEvent())
    }
}

class DeleteCommand(
    context: Context,
    override val affectedEntity: Entity,
    mapEntity: Entity
) : Command() {
    private val addCommand = AddCommand(context, affectedEntity, mapEntity)

    override fun execute() {
        addCommand.unexecute()
    }

    override fun unexecute() {
        addCommand.execute()
    }
}

/**
 * Resizes the [affectedEntity]. If the entity's center position also changed after it was resized, values should be
 * given to [deltaX] and [deltaY]. This is not done in an additional [MoveCommand] because undo() would
 * then have to be called two times (or the undo button pressed two times), instead of once.
 */
class ResizeCommand(
    override val affectedEntity: Entity,
    private val deltaWidth: Float, private val deltaHeight: Float,
    private val deltaX: Float = 0f, private val deltaY: Float = 0f
) : Command() {
    init {
        check(affectedEntity.tryGet(Scene2DComponent) != null)
        { "The [affectedEntity] must have an [ImageComponent]." }
    }

    override fun execute() {
        val newCenterX = affectedEntity.scene2D.centerX + deltaX
        val newCenterY = affectedEntity.scene2D.centerY + deltaY
        affectedEntity.scene2D.run {
            width += deltaWidth
            height += deltaHeight
            centerX = newCenterX
            centerY = newCenterY
        }
        if (affectedEntity.tryGet(MovingObjectComponent) != null) {
            affectedEntity.linkedEntity.get("mockPlatform").scene2D.run {
                width += deltaWidth
                height += deltaHeight
                group.children.first().width += deltaWidth
                group.children.first().height += deltaHeight
                centerX += deltaX - deltaWidth / 2f
                centerY += deltaY - deltaHeight / 2f
            }
        }
    }

    override fun unexecute() {
        val newCenterX = affectedEntity.scene2D.centerX - deltaX
        val newCenterY = affectedEntity.scene2D.centerY - deltaY
        affectedEntity.scene2D.run {
            width -= deltaWidth
            height -= deltaHeight
            group.children.first().width -= deltaWidth
            group.children.first().height -= deltaHeight
            centerX = newCenterX
            centerY = newCenterY
        }
        if (affectedEntity.tryGet(MovingObjectComponent) != null) {
            affectedEntity.linkedEntity.get("mockPlatform").scene2D.run {
                width -= deltaWidth
                height -= deltaHeight
                group.children.first().width -= deltaWidth
                group.children.first().height -= deltaHeight
                centerX -= deltaX - deltaWidth / 2f
                centerY -= deltaY - deltaHeight / 2f
            }
        }
    }
}

class MakeObjectDestroyableCommand(
    private val context: Context,
    override val affectedEntity: Entity
) : Command() {
    // Injected objects
    private val eventQueue: EventQueue = context.inject()

    override fun execute() {
        affectedEntity.run {
            removeComponent<PlatformComponent>()
            destroyablePlatform(context)
            eventQueue.add(UpdateRoundedPlatformsEvent())
        }
    }

    override fun unexecute() {
        affectedEntity.run {
            removeComponent<DestroyablePlatformComponent>()
            platform(context)
            eventQueue.add(UpdateRoundedPlatformsEvent())
        }
    }
}

class MakeObjectNonDestroyableCommand(
    context: Context,
    override val affectedEntity: Entity
) : Command() {
    private val makeDestroyable = MakeObjectDestroyableCommand(context, affectedEntity)

    override fun execute() {
        makeDestroyable.unexecute()
    }

    override fun unexecute() {
        makeDestroyable.execute()
    }
}

class MakeObjectMovingCommand(
    private val context: Context,
    override val affectedEntity: Entity
) : Command() {
    private val engine: PooledEngine = context.inject()
    private val eventQueue: EventQueue = context.inject()
    private var oldSpeed = MovingObjectComponent.SPEED

    override fun execute() {
        affectedEntity.run {
            movingObject(context, scene2D.centerX + 1f, scene2D.centerY + 1f, oldSpeed)
            val mockPlatform = MovingMockPlatformEntity.createEntity(
                context, this,
                movingObject.endPoint.x, movingObject.endPoint.y,
                scene2D.width, scene2D.rotation
            ).apply {
                linkedEntity(context, "platform", this@run)
            }
            linkedEntity(context, "mockPlatform", mockPlatform)

            val dashedLine = DashedLineEntity.createEntity(context, this, mockPlatform)
            mockPlatform.linkedEntity.add("dashedLine", dashedLine)
            this.linkedEntity.add("dashedLine", dashedLine)

            eventQueue.add(UpdateRoundedPlatformsEvent())
        }
    }

    override fun unexecute() {
        affectedEntity.run {
            oldSpeed = movingObject.speed
            removeComponent<MovingObjectComponent>()
            engine.removeEntity(linkedEntity.get("mockPlatform"))
            engine.removeEntity(linkedEntity.get("dashedLine"))
            removeComponent<LinkedEntityComponent>()
        }

        eventQueue.add(UpdateRoundedPlatformsEvent())
    }
}

class MakeObjectNonMovingCommand(
    context: Context,
    override val affectedEntity: Entity
) : Command() {
    private val makeMoving = MakeObjectMovingCommand(context, affectedEntity)

    override fun execute() {
        makeMoving.unexecute()
    }

    override fun unexecute() {
        makeMoving.execute()
    }
}

class MakeObjectRotatingCommand(
    private val context: Context,
    override val affectedEntity: Entity
) : Command() {
    override fun execute() {
        affectedEntity.run {
            rotatingObject(context)
            rotatingIndicator(context)
        }
    }

    override fun unexecute() {
        affectedEntity.run {
            removeComponent<RotatingObjectComponent>()
            removeComponent<RotatingIndicatorComponent>()
        }
    }
}

class MakeObjectNonRotatingCommand(
    context: Context,
    override val affectedEntity: Entity
) : Command() {
    private val makeRotating = MakeObjectRotatingCommand(context, affectedEntity)

    override fun execute() {
        makeRotating.unexecute()
    }

    override fun unexecute() {
        makeRotating.execute()
    }
}