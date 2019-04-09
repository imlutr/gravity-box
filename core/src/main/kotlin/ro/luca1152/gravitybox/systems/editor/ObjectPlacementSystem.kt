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
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.editor.MovingMockPlatformEntity
import ro.luca1152.gravitybox.entities.game.CollectiblePointEntity
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.screenToWorldCoordinates
import ro.luca1152.gravitybox.utils.ui.button.ButtonType
import ro.luca1152.gravitybox.utils.ui.button.PaneButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Places objects at touch when the place tool is used. */
class ObjectPlacementSystem(
    private val inputMultiplexer: InputMultiplexer = Injekt.get(),
    private val uiStage: UIStage = Injekt.get()
) : EntitySystem() {
    private lateinit var undoRedoEntity: Entity
    private lateinit var inputEntity: Entity
    private lateinit var mapEntity: Entity

    private val inputAdapter = object : InputAdapter() {
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            if (!placeToolIsUsed())
                return false

            createPlatformAt(screenX, screenY)
            uiStage.root.findActor<PaneButton>("PaneButton")?.clickedOnButtonFromPane()

            return true
        }

        private fun placeToolIsUsed() =
            inputEntity.input.toggledButton.get()?.type == ButtonType.PLACE_TOOL_BUTTON

        private fun createPlatformAt(screenX: Int, screenY: Int) {
            val coords = screenToWorldCoordinates(screenX, screenY)
            val platformWidth = 1f
            val id = engine.getEntitiesFor(Family.all(MapObjectComponent::class.java).get())
                .filter { !it.editorObject.isDeleted }.size
            val placedEntity = when (inputEntity.input.placeToolObjectType) {
                PlatformComponent::class.java, DestroyablePlatformComponent::class.java, MovingObjectComponent::class.java -> {
                    PlatformEntity.createEntity(
                        id,
                        MathUtils.floor(coords.x).toFloat() + .5f,
                        MathUtils.floor(coords.y).toFloat() + .5f,
                        platformWidth,
                        isDestroyable = inputEntity.input.placeToolObjectType == DestroyablePlatformComponent::class.java
                    )
                }
                CollectiblePointComponent::class.java -> {
                    CollectiblePointEntity.createEntity(
                        id,
                        MathUtils.floor(coords.x).toFloat() + .5f,
                        MathUtils.floor(coords.y).toFloat() + .5f,
                        blinkEndlessly = false
                    )
                }
                else -> error("placeToolObjectType was not recognized.")
            }

            // Place the mock moving platform in the level editor
            if (inputEntity.input.placeToolObjectType == MovingObjectComponent::class.java) {
                val mockPlatform = MovingMockPlatformEntity.createEntity(
                    placedEntity,
                    placedEntity.scene2D.centerX + 1f, placedEntity.scene2D.centerY + 1f,
                    placedEntity.scene2D.width, placedEntity.scene2D.rotation
                )
                placedEntity.linkedEntity("mockPlatform", mockPlatform)
                placedEntity.movingObject(mockPlatform.scene2D.centerX, mockPlatform.scene2D.centerY)
            }

            mapEntity.map.updateRoundedPlatforms = true
            undoRedoEntity.undoRedo.addExecutedCommand(AddCommand(placedEntity, mapEntity))
        }
    }

    override fun addedToEngine(engine: Engine) {
        undoRedoEntity = engine.getSingleton<UndoRedoComponent>()
        inputEntity = engine.getSingleton<InputComponent>()
        mapEntity = engine.getSingleton<MapComponent>()
        inputMultiplexer.addProcessor(inputAdapter)
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(inputAdapter)
    }
}