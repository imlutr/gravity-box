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
import com.badlogic.gdx.graphics.g2d.Batch
import ro.luca1152.gravitybox.utils.GameCamera
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class ImageRenderingSystem(private val batch: Batch = Injekt.get(),
                           private val gameViewport: GameViewport = Injekt.get(),
                           private val gameCamera: GameCamera = Injekt.get(),
                           private val gameStage: GameStage = Injekt.get()) : EntitySystem() {
    override fun update(deltaTime: Float) {
        // Equivalent to an update().
        // It is here since if the images are not rendered, then there is no point in updating them.
        gameStage.act()

        gameViewport.apply()
        batch.projectionMatrix = gameCamera.combined
        drawImages()
    }

    private fun drawImages() {
        repositionImages()
        gameStage.draw()
        repositionImages(restore = true)
    }

    /**
     * Reposition the actors so they are drawn from the center (Box2D bodies' position is from center, not bottom-left)
     * It subtracts half the size of the images and adds it back when restoring.
     */
    private fun repositionImages(restore: Boolean = false) {
        for (i in 0 until gameStage.actors.size) {
            val change = if (restore) 1 else -1
            gameStage.actors[i].x += change * gameStage.actors[i].width / 2f
            gameStage.actors[i].y += change * gameStage.actors[i].height / 2f
        }
    }
}