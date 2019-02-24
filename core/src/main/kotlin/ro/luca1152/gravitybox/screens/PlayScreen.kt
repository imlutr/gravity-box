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
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.components.MapComponent.Companion.GRAVITY
import ro.luca1152.gravitybox.components.map
import ro.luca1152.gravitybox.entities.EntityFactory
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.listeners.CollisionBoxListener
import ro.luca1152.gravitybox.listeners.GameInputListener
import ro.luca1152.gravitybox.listeners.WorldContactListener
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.box2d.MapBodyBuilder
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.GameViewport
import ro.luca1152.gravitybox.utils.ui.ColorScheme.currentLightColor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

class PlayScreen(
        private val engine: PooledEngine = Injekt.get(),
        private val gameViewport: GameViewport = Injekt.get(),
        private val gameStage: GameStage = Injekt.get()) : KtxScreen {
    private val world = World(Vector2(0f, GRAVITY), true)
    private val gameEventSignal = Signal<GameEvent>()

    init {
        // Dependency injection
        Injekt.run {
            addSingleton(world)
            addSingleton(gameEventSignal)
        }

        // Provide own implementation for what happens after collisions
        world.setContactListener(WorldContactListener())
    }

    override fun show() {
        // Create entities
        val mapEntity = EntityFactory.createMap(LevelSelectorScreen.chosenLevel)
        val finishEntity = EntityFactory.createFinish(MapBodyBuilder.buildFinishBody(mapEntity.map.tiledMap))
        val playerEntity = EntityFactory.createPlayer(MapBodyBuilder.buildPlayerBody(mapEntity.map.tiledMap))

        // Add systems
        engine.run {
            addSystem(LevelSystem(mapEntity, finishEntity, playerEntity))
            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(BulletCollisionSystem())
            addSystem(CollisionBoxListener())
            addSystem(PlatformRemovalSystem())
            addSystem(PointSystem(mapEntity.map))
            addSystem(LevelAutoRestartSystem())
            addSystem(ColorSchemeSystem(mapEntity))
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem(mapEntity, playerEntity))
            addSystem(UpdateGameCameraSystem())
            addSystem(MapRenderingSystem(mapEntity))
//            addSystem(PhysicsDebugRenderingSystem())
            addSystem(ImageRenderingSystem())
        }

        // Handle input
        Gdx.input.inputProcessor = GameInputListener(playerEntity)
    }

    override fun render(delta: Float) {
        clearScreen(currentLightColor.r, currentLightColor.g, currentLightColor.b)
        engine.update(delta) // This MUST be after clearScreen() because draw functions may be called in engine.update()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun dispose() {
        gameStage.dispose()
    }
}