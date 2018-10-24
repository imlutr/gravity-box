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

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.components.map
import ro.luca1152.gravitybox.entities.FinishEntity
import ro.luca1152.gravitybox.entities.MapEntity
import ro.luca1152.gravitybox.entities.PlayerEntity
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.listeners.GameInputListener
import ro.luca1152.gravitybox.listeners.WorldContactListener
import ro.luca1152.gravitybox.systems.*
import ro.luca1152.gravitybox.utils.ColorScheme.lightColor
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

class PlayScreen(private val engine: Engine = Injekt.get(),
                 private val gameViewport: GameViewport = Injekt.get()) : KtxScreen {
    private val stage = GameStage
    private val world = World(Vector2(0f, MapEntity.GRAVITY), true)

    init {
        Injekt.run {
            addSingleton(stage)
            addSingleton(world)
        }
    }

    override fun show() {
        val gameEventSignal = Signal<GameEvent>()
        val mapEntity = MapEntity(2)
        Injekt.addSingleton(mapEntity.map.tiledMap)
        val playerEntity = PlayerEntity()
        val finishEntity = FinishEntity()
        world.setContactListener(WorldContactListener(gameEventSignal))
        Injekt.run {
            addSingleton(playerEntity)
            addSingleton(finishEntity)
        }
        Gdx.input.inputProcessor = GameInputListener()

        // Entities
        engine.run {
            addEntity(mapEntity)
            addEntity(playerEntity)
            addEntity(finishEntity)
        }
        // Systems
        engine.run {
            // Physics
            addSystem(PhysicsSystem(world))
            addSystem(PhysicsSyncSystem())

            // Collision
            addSystem(BulletCollisionSystem())

            // Level
            addSystem(AutoRestartSystem(gameEventSignal))
            addSystem(LevelSystem(gameEventSignal))

            // Camera
            addSystem(PlayerCameraSystem(playerEntity, mapEntity))

            // Render
            addSystem(MapRenderSystem(mapEntity.map.tiledMap))
            addSystem(ImageRenderSystem(stage))
//            addSystem(PhysicsDebugSystem(world))
        }
    }

    override fun render(delta: Float) {
        clearScreen(lightColor.r, lightColor.g, lightColor.b)
        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }
}