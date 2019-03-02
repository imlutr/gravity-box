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
import ro.luca1152.gravitybox.components.editor.InputComponent
import ro.luca1152.gravitybox.components.editor.SelectedObjectComponent
import ro.luca1152.gravitybox.components.editor.input
import ro.luca1152.gravitybox.components.editor.selectedObject
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.hitScreen
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import ro.luca1152.gravitybox.utils.ui.ButtonType
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Updates the selected object (the entity that has a [SelectedObjectComponent]) when a platform is touched. */
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
            if (touchedActor == null)
                return false

            // Return true so touchUp() will be called
            return true
        }

        override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (inputEntity.input.isPanning || inputEntity.input.isZooming)
                return true

            if (!isMapObject(touchedActor)) {
                if (selectedObject != null) deselectObject(selectedObject!!)
                return true
            }

            val touchedObject = (touchedActor!!.userObject) as Entity
            if (selectedObject != null && touchedObject != selectedObject)
                deselectObject(selectedObject!!)

            when (touchedObject.tryGet(SelectedObjectComponent)) {
                null -> selectObject(touchedObject)
                else -> {
                    if (selectedObject!!.selectedObject.level == 1) selectedObject!!.selectedObject.level = 2
                    else selectedObject!!.selectedObject.level = 1
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

    private fun selectObject(selectedObject: Entity) {
        selectedObject.run {
            add(engine.createComponent(SelectedObjectComponent::class.java))
        }
    }

    private fun deselectObject(selectedObject: Entity) {
        selectedObject.run {
            remove(SelectedObjectComponent::class.java)
        }
    }

    override fun update(deltaTime: Float) {
        selectedObject = findSelectedObject()
    }

    private fun findSelectedObject(): Entity? {
        val entities = engine.getEntitiesFor(Family.all(SelectedObjectComponent::class.java).get())
        check(entities.size() <= 1) { "There can't be more than one selected object." }

        return if (entities.size() == 1) entities.first() else null
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(inputAdapter)
    }
}