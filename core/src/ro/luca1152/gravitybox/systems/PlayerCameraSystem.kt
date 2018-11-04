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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import ro.luca1152.gravitybox.components.image
import ro.luca1152.gravitybox.components.map
import ro.luca1152.gravitybox.utils.GameCamera
import ro.luca1152.gravitybox.utils.lerp
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/**
 * Make the game [gameCamera] follow the [playerEntity].
 */
class PlayerCameraSystem(
    private val mapEntity: Entity,
    private val playerEntity: Entity,
    private val gameCamera: GameCamera = Injekt.get()
) : EntitySystem() {
    override fun update(deltaTime: Float) {
        // Smoothly move the camera towards the player
        gameCamera.position.lerp(
            playerEntity.image.x + playerEntity.image.width / 2f,
            playerEntity.image.y + playerEntity.image.height / 2f,
            progress = .15f
        )

        // Keep the camera within the bounds of the map
        keepWithinBounds(gameCamera.zoom)

        // Apply the changes
        gameCamera.update()
    }

    private fun keepWithinBounds(zoom: Float) {
        var mapLeft = 0f
        var mapRight = mapEntity.map.width * zoom
        if (mapEntity.map.width * zoom > gameCamera.viewportWidth * zoom) {
            mapLeft = (-1) * zoom
            mapRight = (mapEntity.map.width + 1) * zoom
        }
        val mapBottom = 0 * zoom
        val mapTop = mapEntity.map.height * zoom
        val cameraHalfWidth = gameCamera.viewportWidth / 2f * zoom
        val cameraHalfHeight = gameCamera.viewportHeight / 2f * zoom
        val cameraLeft = gameCamera.position.x - cameraHalfWidth
        val cameraRight = gameCamera.position.x + cameraHalfWidth
        val cameraBottom = gameCamera.position.y - cameraHalfHeight
        val cameraTop = gameCamera.position.y + cameraHalfHeight

        // Clamp horizontal axis
        when {
            gameCamera.viewportWidth * zoom > mapRight -> gameCamera.position.x = mapRight / 2f
            cameraLeft <= mapLeft -> gameCamera.position.x = mapLeft + cameraHalfWidth
            cameraRight >= mapRight -> gameCamera.position.x = mapRight - cameraHalfWidth
        }

        // Clamp vertical axis
        when {
            gameCamera.viewportHeight * zoom > mapTop -> gameCamera.position.y = mapTop / 2f
            cameraBottom <= mapBottom -> gameCamera.position.y = mapBottom + cameraHalfHeight
            cameraTop >= mapTop -> gameCamera.position.y = mapTop - cameraHalfHeight
        }
    }
}
