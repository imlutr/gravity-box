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
import com.badlogic.gdx.physics.box2d.Box2D
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
import ro.luca1152.gravitybox.systems.*
import ro.luca1152.gravitybox.utils.ColorScheme.currentLightColor
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.GameViewport
import ro.luca1152.gravitybox.utils.MapBodyBuilder
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

class PlayScreen(
        private val engine: PooledEngine = Injekt.get(),
        private val gameViewport: GameViewport = Injekt.get()
) : KtxScreen {
    private val world = World(Vector2(0f, GRAVITY), true)
    private val gameEventSignal = Signal<GameEvent>()
    private val stage = GameStage

    init {
        // Load the Box2D native library
        Box2D.init()

        // Dependency injection
        Injekt.run {
            addSingleton(stage)
            addSingleton(world)
            addSingleton(gameEventSignal)
        }

        world.setContactListener(WorldContactListener(gameEventSignal))
    }

    override fun show() {
        // Create entities
        val mapEntity = EntityFactory.createMap(LevelSelectorScreen.chosenLevel)
        val playerEntity = EntityFactory.createPlayer(MapBodyBuilder.buildPlayerBody(mapEntity.map.tiledMap))
        val finishEntity = EntityFactory.createFinish(MapBodyBuilder.buildFinishBody(mapEntity.map.tiledMap))

        // Handle input
        Gdx.input.inputProcessor = GameInputListener(playerEntity)
        engine.run {
            addSystem(LevelSystem(mapEntity, finishEntity, playerEntity))
            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(BulletCollisionSystem(playerEntity))
            addSystem(CollisionBoxListener())
            addSystem(PlatformRemovalSystem())
            addSystem(PointSystem(mapEntity.map))
            addSystem(AutoRestartSystem())
            addSystem(ColorSchemeSystem(mapEntity))
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem(mapEntity, playerEntity))
            addSystem(RenderSystem(mapEntity))
        }
    }

    override fun render(delta: Float) {
        clearScreen(currentLightColor.r, currentLightColor.g, currentLightColor.b)
        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }
}