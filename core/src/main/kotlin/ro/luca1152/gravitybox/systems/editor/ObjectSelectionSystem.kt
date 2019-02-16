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

package ro.luca1152.gravitybox.systems.editor

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import ro.luca1152.gravitybox.components.ColorType
import ro.luca1152.gravitybox.components.SelectedObjectComponent
import ro.luca1152.gravitybox.components.color
import ro.luca1152.gravitybox.components.input
import ro.luca1152.gravitybox.components.utils.tryGet
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.ui.ButtonType
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Updates the selected object (the entity that has a [SelectedObjectComponent]) when a platform is touched. */
class ObjectSelectionSystem(private val inputEntity: Entity,
                            private val inputMultiplexer: InputMultiplexer = Injekt.get(),
                            private val gameStage: GameStage = Injekt.get()) : EntitySystem() {
    var selectedObject: Entity? = null

    private val inputAdapter = object : InputAdapter() {
        var touchedActor: Actor? = null

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (inputEntity.input.isPanning || inputEntity.input.isZooming) {
                return false
            }

            if (!moveToolIsSelected())
                return false

            val stageCoords = gameStage.screenToStageCoordinates(Vector2(screenX.toFloat(), screenY.toFloat()))
            touchedActor = gameStage.hit(stageCoords.x, stageCoords.y, true)

            if (touchedActor == null)
                return false

            // Return true so touchUp() will be called
            return true
        }

        private fun moveToolIsSelected() = inputEntity.input.toggledButton.get()?.type == ButtonType.MOVE_TOOL_BUTTON

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (inputEntity.input.isPanning || inputEntity.input.isZooming) {
                return false
            }

            if (!isMapObject(touchedActor)) {
                if (selectedObject != null)
                    unselectObject(selectedObject!!)
                return true
            }
            val entity = (touchedActor!!.userObject) as Entity

            if (selectedObject != null && entity != selectedObject)
                unselectObject(selectedObject!!)

            entity.run {
                when (tryGet(SelectedObjectComponent) != null) {
                    true -> unselectObject(entity)
                    false -> selectObject(entity)
                }
            }

            return true
        }

        private fun isMapObject(actor: Actor?) = (actor != null && actor.userObject != null && actor.userObject is Entity)
    }

    override fun addedToEngine(engine: Engine?) {
        inputMultiplexer.addProcessor(inputAdapter)
    }

    private fun selectObject(selectedObject: Entity) {
        selectedObject.run {
            add(engine.createComponent(SelectedObjectComponent::class.java))
            color.colorType = ColorType.DARKER_DARK
        }
    }

    private fun unselectObject(selectedObject: Entity) {
        selectedObject.run {
            remove(SelectedObjectComponent::class.java)
            color.colorType = ColorType.DARK
        }
    }

    override fun update(deltaTime: Float) {
        selectedObject = findSelectedObject()
    }

    private fun findSelectedObject(): Entity? {
        val entities = engine.getEntitiesFor(Family.all(SelectedObjectComponent::class.java).get())
        return when (entities.size()) {
            0 -> null
            1 -> entities.first()
            else -> error { "More than one selected platform." }
        }
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(inputAdapter)
    }
}