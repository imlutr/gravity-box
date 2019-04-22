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
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.MathUtils
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.InputComponent
import ro.luca1152.gravitybox.components.editor.input
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.ui.button.ButtonType

/** Handles zoom gestures. */
class ZoomingSystem(context: Context) : EntitySystem() {
    private val gameCamera: GameCamera = context.inject()
    private val inputMultiplexer: InputMultiplexer = context.inject()

    private lateinit var inputEntity: Entity

    companion object {
        private const val DEFAULT_ZOOM = .75f
        private const val MIN_ZOOM = .3f // The maximum you can zoom IN
        private const val MAX_ZOOM = 1.5f // The maximum you can zoom OUT
    }

    private var currentZoom = DEFAULT_ZOOM
    private var isRightBracketPressed = false
    private var isLeftBracketPressed = false

    private val gestureDetector = GestureDetector(object : GestureDetector.GestureAdapter() {
        override fun zoom(initialDistance: Float, distance: Float): Boolean {
            if (!moveToolIsUsed())
                return false

            if (liftedOneFinger()) {
                currentZoom = gameCamera.zoom
                return true
            }

            inputEntity.input.isZooming = true
            gameCamera.zoom = currentZoom * (initialDistance / distance)

            // If it return true, there would occasionally be a sudden pan after lifting both fingers
            return false
        }

        override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
            inputEntity.input.isPanning = false
            inputEntity.input.isZooming = false
            currentZoom = gameCamera.zoom
            return true
        }

        private fun moveToolIsUsed() = inputEntity.input.toggledButton.get()?.type == ButtonType.MOVE_TOOL_BUTTON

        private fun liftedOneFinger() = !Gdx.input.isTouched(1) || (!Gdx.input.isTouched(0) && Gdx.input.isTouched(1))
    })

    private val keyListener = object : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            when (keycode) {
                Input.Keys.RIGHT_BRACKET -> isRightBracketPressed = true
                Input.Keys.LEFT_BRACKET -> isLeftBracketPressed = true
                else -> return false
            }
            return true
        }

        override fun keyUp(keycode: Int): Boolean {
            when (keycode) {
                Input.Keys.RIGHT_BRACKET -> isRightBracketPressed = false
                Input.Keys.LEFT_BRACKET -> isLeftBracketPressed = false
                Input.Keys.SPACE -> gameCamera.zoom = 1f
                else -> return false
            }
            return true
        }
    }

    override fun addedToEngine(engine: Engine) {
        inputEntity = engine.getEntitiesFor(Family.all(InputComponent::class.java).get()).first()
        if (gameCamera.zoom == 0f) {
            gameCamera.zoom = DEFAULT_ZOOM
        }
        inputMultiplexer.addProcessor(gestureDetector)
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