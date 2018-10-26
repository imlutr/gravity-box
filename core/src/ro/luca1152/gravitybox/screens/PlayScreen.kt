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
import ro.luca1152.gravitybox.entities.FinishEntity
import ro.luca1152.gravitybox.entities.MapEntity
import ro.luca1152.gravitybox.entities.MapEntity.Companion.GRAVITY
import ro.luca1152.gravitybox.entities.PlayerEntity
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.listeners.CollisionBoxListener
import ro.luca1152.gravitybox.listeners.GameInputListener
import ro.luca1152.gravitybox.listeners.WorldContactListener
import ro.luca1152.gravitybox.systems.*
import ro.luca1152.gravitybox.utils.ColorScheme.currentLightColor
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

class PlayScreen(private val engine: Engine = Injekt.get(),
                 private val gameViewport: GameViewport = Injekt.get()) : KtxScreen {
    private val world = World(Vector2(0f, GRAVITY), true)
    private val gameEventSignal = Signal<GameEvent>()
    private val stage = GameStage

    init {
        Injekt.run { addSingleton(stage); addSingleton(world); addSingleton(gameEventSignal) }
        world.setContactListener(WorldContactListener(gameEventSignal))
    }

    override fun show() {
        // Create entities
        val mapEntity = MapEntity(1)
        Injekt.run { addSingleton(mapEntity) }
        val finishEntity = FinishEntity()
        val playerEntity = PlayerEntity()
        Injekt.run { addSingleton(playerEntity); addSingleton(finishEntity) }

        // Handle input
        Gdx.input.inputProcessor = GameInputListener()

        engine.run {
            addEntity(mapEntity)
            addEntity(finishEntity)
            addEntity(playerEntity)

            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(BulletCollisionSystem())
            addSystem(CollisionBoxListener())
            addSystem(AutoRestartSystem())
            addSystem(LevelSystem())
            addSystem(ColorSchemeSystem())
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem())
            addSystem(RenderSystem())
        }
    }

    override fun render(delta: Float) {
        clearScreen(currentLightColor.r, currentLightColor.g, currentLightColor.b)
        engine.update(delta)
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }
}