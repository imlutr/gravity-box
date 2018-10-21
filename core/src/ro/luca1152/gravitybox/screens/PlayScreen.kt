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
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.components.map
import ro.luca1152.gravitybox.entities.FinishEntity
import ro.luca1152.gravitybox.entities.MapEntity
import ro.luca1152.gravitybox.entities.PlayerEntity
import ro.luca1152.gravitybox.systems.*
import ro.luca1152.gravitybox.utils.ColorScheme.lightColor
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlayScreen(private val engine: Engine = Injekt.get(),
                 private val gameViewport: GameViewport = Injekt.get(),
                 batch: Batch = Injekt.get()) : KtxScreen {
    private val stage = Stage(gameViewport, batch)
    private val world = World(Vector2(0f, -1f), true)

    override fun show() {
        val mapEntity = MapEntity(1, world)
        val playerEntity = PlayerEntity(mapEntity.map.tiledMap, world, stage)
        val finishEntity = FinishEntity(mapEntity.map.tiledMap, world, stage)
        engine.run {
            // Entities
            addEntity(mapEntity)
            addEntity(finishEntity)
            addEntity(playerEntity)
            // Systems
            addSystem(PhysicsSystem(world))
            addSystem(PhysicsSynchronizationSystem())
            addSystem(PlayerCameraSystem(playerEntity, mapEntity))
            addSystem(MapRenderingSystem(mapEntity.map.tiledMap))
            addSystem(ImageRenderingSystem(stage))
            addSystem(PhysicsDebugSystem(world))
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