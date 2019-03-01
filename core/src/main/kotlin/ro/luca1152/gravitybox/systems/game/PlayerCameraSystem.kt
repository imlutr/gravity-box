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

package ro.luca1152.gravitybox.systems.game

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.math.Vector3
import ro.luca1152.gravitybox.components.LevelComponent
import ro.luca1152.gravitybox.components.PlayerComponent
import ro.luca1152.gravitybox.components.image
import ro.luca1152.gravitybox.components.newMap
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.kotlin.lerp
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Makes the game [gameCamera] follow the [playerEntity]. */
class PlayerCameraSystem(private val gameCamera: GameCamera = Injekt.get()) : EntitySystem() {
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity
    private var initialCameraZoom = 1f
    private var initialCameraPosition = Vector3()

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get())
        playerEntity = engine.getSingletonFor(Family.all(PlayerComponent::class.java).get())
        initialCameraZoom = gameCamera.zoom
        initialCameraPosition.set(gameCamera.position)
        gameCamera.zoom = 1f
        instantlyCenterCameraOnPlayer()
    }

    private fun instantlyCenterCameraOnPlayer() {
        gameCamera.position.set(playerEntity.image.centerX, playerEntity.image.centerY, 0f)
    }

    override fun update(deltaTime: Float) {
        smoothlyFollowPlayer()
        keepCameraWithinBounds()
    }

    private fun smoothlyFollowPlayer() {
        gameCamera.position.lerp(
            playerEntity.image.centerX,
            playerEntity.image.centerY,
            progress = .15f
        )
    }

    private fun keepCameraWithinBounds(zoom: Float = 1f) {
        val mapWidth = levelEntity.newMap.widthInTiles
        val mapHeight = levelEntity.newMap.heightInTiles

        var mapLeft = 0f
        var mapRight = mapWidth * zoom
        if (mapWidth * zoom > gameCamera.viewportWidth * zoom) {
            mapLeft = (-1) * zoom
            mapRight = (mapWidth + 1) * zoom
        }
        val mapBottom = 0 * zoom
        val mapTop = mapHeight * zoom
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

    override fun removedFromEngine(engine: Engine) {
        resetCameraToInitialState()
    }

    private fun resetCameraToInitialState() {
        gameCamera.run {
            zoom = initialCameraZoom
            position.set(initialCameraPosition)
        }
    }

}
