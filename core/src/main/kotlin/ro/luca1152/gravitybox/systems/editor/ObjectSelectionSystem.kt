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
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.filterNullableSingleton
import ro.luca1152.gravitybox.utils.kotlin.hitAllScreen
import ro.luca1152.gravitybox.utils.ui.button.ButtonType

/** Selects touched map objects. */
class ObjectSelectionSystem(context: Context) : EntitySystem() {
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private val gameStage: GameStage = context.inject()

    private lateinit var inputEntity: Entity
    var selectedObject: Entity? = null

    private val inputAdapter = object : GestureDetector(GestureAdapter()) {
        lateinit var touchedActors: List<Actor>

        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (!moveToolIsSelected()) return false

            touchedActors = gameStage.hitAllScreen(screenX, screenY).filter { isMapObject(it) }.distinctBy { it.userObject }
                .sortedBy { it.zIndex }

            if (touchedActors.isEmpty()) {
                return false
            }

            // Return true so touchUp() will be called
            return true
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (inputEntity.input.isPanning || inputEntity.input.isZooming) return true

            // Sometimes (rarely) touchedActors is not initialized and the game crashes :S
            if (!::touchedActors.isInitialized) return true

            if (!touchedActors.any { it.userObject == selectedObject }) {
                selectedObject?.editorObject?.isSelected = false
                if (touchedActors.isNotEmpty()) {
                    (touchedActors.first().userObject as Entity).run {
                        overlay.overlayLevel = 1
                        editorObject.isSelected = true
                    }
                }
            } else {
                if (selectedObject!!.overlay.overlayLevel == 1) selectedObject!!.overlay.overlayLevel = 2
                else selectedObject!!.overlay.overlayLevel = 1
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