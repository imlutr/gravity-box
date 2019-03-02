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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.utils.Json
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.utils.assets.Text
import ro.luca1152.gravitybox.utils.json.FinishPrototype
import ro.luca1152.gravitybox.utils.json.MapFactory
import ro.luca1152.gravitybox.utils.json.ObjectPrototype
import ro.luca1152.gravitybox.utils.json.PlayerPrototype
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MapLoadingSystem(private val manager: AssetManager = Injekt.get()) : EntitySystem() {
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get())
        playerEntity = engine.getSingletonFor(Family.all(PlayerComponent::class.java).get())
        finishEntity = engine.getSingletonFor(Family.all(FinishComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        if (!levelEntity.level.loadMap)
            return
        loadMap()
    }

    private fun loadMap() {
        levelEntity.run {
            level.loadMap = false
            newMap.destroyAllBodies()
        }
        val jsonData = manager.get<Text>("maps/game/map-1.json").string
        val mapFactory = Json().fromJson(MapFactory::class.java, jsonData)

        createMap(mapFactory.width, mapFactory.height, mapFactory.id)
        createPlayer(mapFactory.player)
        createFinish(mapFactory.finish)
        createPlatforms(mapFactory.objects)
    }

    private fun createMap(width: Int, height: Int, id: Int) {
        levelEntity.run {
            newMap.run {
                levelNumber = id
                widthInTiles = width.pixelsToMeters.toInt()
                heightInTiles = height.pixelsToMeters.toInt()
            }
        }
    }

    private fun createPlayer(player: PlayerPrototype) {
        playerEntity.run {
            image.run {
                centerX = player.position.x.pixelsToMeters
                centerY = player.position.y.pixelsToMeters
            }
            newMapObject.run {
                id = player.id
            }
        }
    }

    private fun createFinish(finish: FinishPrototype) {
        finishEntity.run {
            image.run {
                centerX = finish.position.x.pixelsToMeters
                centerY = finish.position.y.pixelsToMeters
            }
            newMapObject.run {
                id = finish.id
            }
        }
    }

    private fun createPlatforms(objects: ArrayList<ObjectPrototype>) {
        objects.forEach {
            when {
                it.type == "platform" -> PlatformEntity.createEntity(
                    it.id,
                    it.position.x.pixelsToMeters,
                    it.position.y.pixelsToMeters,
                    it.width.pixelsToMeters
                )
            }
        }
    }
}