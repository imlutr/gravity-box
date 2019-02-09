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
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.MathUtils
import ro.luca1152.gravitybox.components.buttonListener
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.ui.ButtonType
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ZoomingSystem(private val buttonListenerEntity: Entity,
                    private val gameCamera: GameCamera = Injekt.get(),
                    private val inputMultiplexer: InputMultiplexer = Injekt.get()) : EntitySystem() {
    companion object {
        private const val DEFAULT_ZOOM = .75f
        private const val MIN_ZOOM = .3f // The maximum you can zoom in
        private const val MAX_ZOOM = 1.5f // The maximum you can zoom out
    }

    private lateinit var gestureDetector: GestureDetector
    private lateinit var keyListener: InputAdapter
    private var currentZoom = DEFAULT_ZOOM
    private var isRightBracketPressed = false
    private var isLeftBracketPressed = false

    override fun addedToEngine(engine: Engine?) {
        gameCamera.zoom = DEFAULT_ZOOM

        // Add gesture listener for zooming
        gestureDetector = GestureDetector(object : GestureDetector.GestureAdapter() {
            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                // If the move tool isn't in use, then you can't zoom
                if (buttonListenerEntity.buttonListener.toggledButton.get()?.type != ButtonType.MOVE_TOOL_BUTTON)
                    return false

                // If a finger was lifted then zooming should stop and the currentZoom should be updated, since it is
                // updated only when zooming stops, and you can't zoom with only one finger.
                if (!Gdx.input.isTouched(1) || (!Gdx.input.isTouched(0) && Gdx.input.isTouched(1))) {
                    currentZoom = gameCamera.zoom
                    return true
                }

                // Apply the actual zoom
                gameCamera.zoom = currentZoom * (initialDistance / distance)

                // If true, the zooming gesture gets worse, meaning that there would occasionally be
                // a sudden pan after you stop zooming, so I just return false.
                return false
            }

            override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
                // Update the currentZoom here because if it was updated in zoom() you would zoom exponentially.
                currentZoom = gameCamera.zoom

                return true
            }
        })
        inputMultiplexer.addProcessor(gestureDetector)

        keyListener = object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean {
                when (keycode) {
                    Input.Keys.RIGHT_BRACKET -> {
                        isRightBracketPressed = true
                        return true
                    }
                    Input.Keys.LEFT_BRACKET -> {
                        isLeftBracketPressed = true
                        return true
                    }
                }
                return false
            }

            override fun keyUp(keycode: Int): Boolean {
                when (keycode) {
                    Input.Keys.RIGHT_BRACKET -> {
                        isRightBracketPressed = false
                        return true
                    }
                    Input.Keys.LEFT_BRACKET -> {
                        isLeftBracketPressed = false
                        return true
                    }
                    Input.Keys.SPACE -> {
                        gameCamera.zoom = 1f
                    }
                }
                return false
            }
        }
        inputMultiplexer.addProcessor(keyListener)
    }

    override fun update(deltaTime: Float) {
        zoomFromKeyboard()
        keepZoomWithinBounds()
    }

    private fun zoomFromKeyboard() {
        when {
            isRightBracketPressed -> gameCamera.zoom /= 1.035f
            isLeftBracketPressed -> gameCamera.zoom *= 1.035f
        }
    }

    private fun keepZoomWithinBounds() {
        gameCamera.zoom = MathUtils.clamp(gameCamera.zoom, MIN_ZOOM, MAX_ZOOM)
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(gestureDetector)
        inputMultiplexer.removeProcessor(keyListener)
    }
}