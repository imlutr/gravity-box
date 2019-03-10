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

package ro.luca1152.gravitybox.screens

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.LevelEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.listeners.CollisionBoxListener
import ro.luca1152.gravitybox.listeners.WorldContactListener
import ro.luca1152.gravitybox.systems.editor.SelectedObjectColorSystem
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.GameViewport
import ro.luca1152.gravitybox.utils.ui.ColorScheme.currentLightColor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlayScreen(
    private val engine: PooledEngine = Injekt.get(),
    private val gameViewport: GameViewport = Injekt.get(),
    private val gameStage: GameStage = Injekt.get(),
    private val world: World = Injekt.get(),
    private val inputMultiplexer: InputMultiplexer = Injekt.get()
) : KtxScreen {
    override fun show() {
        setOwnBox2DContactListener()
        createGameEntities()
        addGameSystems()
        handleAllInput()
    }

    private fun setOwnBox2DContactListener() {
        world.setContactListener(WorldContactListener())
    }

    private fun createGameEntities() {
        LevelEntity.createEntity(LevelSelectorScreen.chosenLevel).run {
            level.loadMap = true
            level.forceUpdateMap = true
        }
        PlayerEntity.createEntity()
        FinishEntity.createEntity()
    }

    private fun addGameSystems() {
        engine.run {
            addSystem(MapLoadingSystem())
            addSystem(MapBodiesCreationSystem())
            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(ShootingSystem())
            addSystem(BulletCollisionSystem())
            addSystem(CollisionBoxListener())
            addSystem(PlatformRemovalSystem())
//            addSystem(PointSystem(mapEntity.map)) TODO
            addSystem(OffScreenLevelRestartSystem())
            addSystem(KeyboardLevelRestartSystem())
            addSystem(LevelFinishDetectionSystem())
            addSystem(LevelFinishSystem())
            addSystem(LevelRestartSystem())
            addSystem(FinishPointColorSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem())
            addSystem(UpdateGameCameraSystem())
            addSystem(ImageRenderingSystem())
//            addSystem(PhysicsDebugRenderingSystem())
        }
    }

    private fun handleAllInput() {
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun render(delta: Float) {
        clearScreen(currentLightColor.r, currentLightColor.g, currentLightColor.b)
        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun dispose() {
        gameStage.dispose()
    }
}