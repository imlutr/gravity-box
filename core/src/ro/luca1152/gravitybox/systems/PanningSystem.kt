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

package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureAdapter
import com.badlogic.gdx.input.GestureDetector.GestureListener
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.utils.GameCamera
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PanningSystem(private val gameCamera: GameCamera = Injekt.get(),
                    private val inputMultiplexer: InputMultiplexer = Injekt.get()) : EntitySystem() {
    private lateinit var gestureListener: GestureListener

    override fun addedToEngine(engine: Engine?) {
        gestureListener = object : GestureAdapter() {
            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                gameCamera.position.add(-deltaX.pixelsToMeters, deltaY.pixelsToMeters, 0f)
                return true
            }
        }
        inputMultiplexer.addProcessor(GestureDetector(gestureListener))
    }


}