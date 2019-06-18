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
import com.badlogic.gdx.math.Vector3
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.components.game.scene2D
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.lerp

/** Makes the game [gameCamera] follow the [playerEntity]. */
class PlayerCameraSystem(
    context: Context,
    private val playScreen: PlayScreen? = null
) : EntitySystem() {
    // Injected objects
    private val gameCamera: GameCamera = context.inject()

    // Entities
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    private var initialCameraZoom = 1f
    private var initialCameraPosition = Vector3()
    private val lerpCameraPosition = Vector3()

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
        initialCameraZoom = gameCamera.zoom
        initialCameraPosition.set(gameCamera.position)
        gameCamera.zoom = 1f
        instantlyCenterCameraOnPlayer()
    }

    private fun instantlyCenterCameraOnPlayer() {
        lerpCameraPosition.set(
            playerEntity.scene2D.centerX,
            playerEntity.scene2D.centerY,
            0f
        )
    }

    override fun update(deltaTime: Float) {
        if (levelEntity.map.forceCenterCameraOnPlayer) {
            instantlyCenterCameraOnPlayer()
            levelEntity.map.forceCenterCameraOnPlayer = false
        } else {
            smoothlyFollowPlayer()
        }
        keepCameraWithinBounds()
        gameCamera.position.set(lerpCameraPosition)
        gameCamera.position.y -= playScreen?.shiftCameraYBy ?: 0f
    }

    private fun smoothlyFollowPlayer() {
        lerpCameraPosition.lerp(
            playerEntity.scene2D.centerX,
            playerEntity.scene2D.centerY,
            progress = .15f
        )
    }

    private fun keepCameraWithinBounds(zoom: Float = 1f) {
        val cameraHalfWidth = gameCamera.viewportWidth / 2f * zoom
        val cameraHalfHeight = gameCamera.viewportHeight / 2f * zoom
        val mapLeft = (levelEntity.map.mapLeft - levelEntity.map.paddingLeft) * zoom
        val mapRight = (levelEntity.map.mapRight + levelEntity.map.paddingRight) * zoom
        val mapBottom = (levelEntity.map.mapBottom - levelEntity.map.paddingBottom) * zoom
        val mapTop = (levelEntity.map.mapTop + levelEntity.map.paddingTop) * zoom
        val mapWidth = Math.abs(mapRight - mapLeft)
        val mapHeight = Math.abs(mapTop - mapBottom)
        val cameraLeft = lerpCameraPosition.x - cameraHalfWidth
        val cameraRight = lerpCameraPosition.x + cameraHalfWidth
        val cameraBottom = lerpCameraPosition.y - cameraHalfHeight
        val cameraTop = lerpCameraPosition.y + cameraHalfHeight

        // Clamp horizontal axis
        when {
            mapWidth < gameCamera.viewportWidth -> lerpCameraPosition.x = mapRight - mapWidth / 2f
            cameraLeft <= mapLeft -> lerpCameraPosition.x = mapLeft + cameraHalfWidth
            cameraRight >= mapRight -> lerpCameraPosition.x = mapRight - cameraHalfWidth
        }

        // Clamp vertical axis
        when {
            mapHeight < gameCamera.viewportHeight -> lerpCameraPosition.y = mapTop - mapHeight / 2f
            cameraBottom <= mapBottom -> lerpCameraPosition.y = mapBottom + cameraHalfHeight
            cameraTop >= mapTop -> lerpCameraPosition.y = mapTop - cameraHalfHeight
        }
    }

    override fun removedFromEngine(engine: Engine) {
        resetCameraToInitialState()
    }

    private fun resetCameraToInitialState() {
        gameCamera.run {
            zoom = initialCameraZoom
            position.set(initialCameraPosition)
            update()
        }
    }

}
