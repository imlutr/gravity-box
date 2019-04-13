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
import com.badlogic.gdx.scenes.scene2d.Actor
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.filterNullableSingleton
import ro.luca1152.gravitybox.utils.kotlin.hitScreen
import ro.luca1152.gravitybox.utils.ui.button.ButtonType
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Selects touched map objects. */
class ObjectSelectionSystem(
    private val inputMultiplexer: InputMultiplexer = Injekt.get(),
    private val gameStage: GameStage = Injekt.get()
) : EntitySystem() {
    private lateinit var inputEntity: Entity
    var selectedObject: Entity? = null

    private val inputAdapter = object : InputAdapter() {
        var touchedActor: Actor? = null

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (!moveToolIsSelected())
                return false

            touchedActor = gameStage.hitScreen(screenX, screenY)
            if (touchedActor == null) {
                selectedObject?.editorObject?.isSelected = false
                return false
            }

            // Return true so touchUp() will be called
            return true
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (inputEntity.input.isPanning || inputEntity.input.isZooming)
                return true

            if (!isMapObject(touchedActor)) {
                selectedObject?.editorObject?.isSelected = false
                return true
            }

            val touchedObject = (touchedActor!!.userObject) as Entity
            if (selectedObject != null && touchedObject != selectedObject) {
                selectedObject!!.editorObject.isSelected = false
            }

            if (touchedObject.editorObject.isSelected) {
                if (selectedObject!!.overlay.overlayLevel == 1) selectedObject!!.overlay.overlayLevel = 2
                else selectedObject!!.overlay.overlayLevel = 1
            } else {
                touchedObject.run {
                    editorObject.isSelected = true
                    overlay.overlayLevel = 1
                }
            }

            return true
        }

        private fun moveToolIsSelected() = inputEntity.input.toggledButton.get()?.type == ButtonType.MOVE_TOOL_BUTTON

        private fun isMapObject(actor: Actor?) =
            (actor != null && actor.userObject != null && actor.userObject is Entity)
    }

    override fun addedToEngine(engine: Engine) {
        inputEntity = engine.getEntitiesFor(Family.all(InputComponent::class.java).get()).first()
        inputMultiplexer.addProcessor(inputAdapter)
    }

    override fun update(deltaTime: Float) {
        updateSelectedObject()
    }

    private fun updateSelectedObject() {
        selectedObject = engine.getEntitiesFor(Family.all(EditorObjectComponent::class.java).get())
            .filterNullableSingleton { it.editorObject.isSelected }
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(inputAdapter)
    }
}