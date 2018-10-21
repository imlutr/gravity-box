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

import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.scenes.scene2d.Stage
import ro.luca1152.gravitybox.utils.GameCamera
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ImageRenderingSystem(private val stage: Stage,
                           private val gameCamera: GameCamera = Injekt.get(),
                           private val gameViewport: GameViewport = Injekt.get()) : EntitySystem() {
    override fun update(deltaTime: Float) {
        gameViewport.apply()
        stage.batch.projectionMatrix = gameCamera.combined
        stage.act()
        repositionActors()
        stage.draw()
        restorePositions()
    }

    /**
     *  Reposition the actors so they are drawn from the center.
     */
    private fun repositionActors() {
        for (i in 0 until stage.actors.size) {
            stage.actors[i].x -= stage.actors[i].width / 2f
            stage.actors[i].y -= stage.actors[i].height / 2f
        }
    }

    private fun restorePositions() {
        for (i in 0 until stage.actors.size) {
            stage.actors[i].x += stage.actors[i].width / 2f
            stage.actors[i].y += stage.actors[i].height / 2f
        }
    }
}