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
import com.badlogic.gdx.math.MathUtils
import ro.luca1152.gravitybox.components.AddCommand
import ro.luca1152.gravitybox.components.UndoRedoComponent
import ro.luca1152.gravitybox.components.input
import ro.luca1152.gravitybox.components.undoRedo
import ro.luca1152.gravitybox.entities.MapObjectFactory
import ro.luca1152.gravitybox.utils.kotlin.screenToWorldCoordinates
import ro.luca1152.gravitybox.utils.ui.ButtonType
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Places objects at touch when the place tool is used. */
class ObjectPlacementSystem(private val buttonListenerEntity: Entity,
                            private val inputMultiplexer: InputMultiplexer = Injekt.get()) : EntitySystem() {
    private lateinit var undoRedoEntity: Entity

    private val inputAdapter = object : InputAdapter() {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (!placeToolIsUsed())
                return false

            createPlatformAt(screenX, screenY)

            return true
        }

        private fun placeToolIsUsed() = buttonListenerEntity.input.toggledButton.get()?.type == ButtonType.PLACE_TOOL_BUTTON

        private fun createPlatformAt(screenX: Int, screenY: Int) {
            val coords = screenToWorldCoordinates(screenX, screenY)
            val platform = MapObjectFactory.createPlatform(0, MathUtils.floor(coords.x).toFloat() + .5f, MathUtils.floor(coords.y).toFloat() + .5f)
            undoRedoEntity.undoRedo.addExecutedCommand(AddCommand(platform))
        }
    }

    override fun addedToEngine(engine: Engine) {
        undoRedoEntity = engine.getEntitiesFor(Family.all(UndoRedoComponent::class.java).get()).first()
        inputMultiplexer.addProcessor(inputAdapter)
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(inputAdapter)
    }
}