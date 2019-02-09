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
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import ro.luca1152.gravitybox.components.buttonListener
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.map.Map
import ro.luca1152.gravitybox.utils.map.objects.PlatformObject
import ro.luca1152.gravitybox.utils.map.objects.PlatformType
import ro.luca1152.gravitybox.utils.ui.ButtonType
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlatformPlacementSystem(private val map: Map,
                              private val buttonListenerEntity: Entity,
                              private val inputMultiplexer: InputMultiplexer = Injekt.get(),
                              private val gameCamera: GameCamera = Injekt.get()) : EntitySystem() {
    private lateinit var inputAdapter: InputAdapter
    private val coords = Vector3()

    override fun addedToEngine(engine: Engine?) {
        inputAdapter = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                // Place platforms only if the place tool is selected
                if (buttonListenerEntity.buttonListener.toggledButton.get()?.type != ButtonType.PLACE_TOOL_BUTTON)
                    return false

                // Translate the screen coordinates to world coordinates
                coords.run {
                    x = screenX.toFloat()
                    y = screenY.toFloat()
                }
                gameCamera.unproject(coords)

                // Create and add the platform to the map
                map.objects.add(PlatformObject(map.objects.size, PlatformType.FULL).apply {
                    x = MathUtils.floor(coords.x).toFloat()
                    y = MathUtils.floor(coords.y) + .5f - PlatformObject.DEFAULT_HEIGHT / 2f
                })

                return true
            }
        }
        inputMultiplexer.addProcessor(inputAdapter)
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(inputAdapter)
    }
}