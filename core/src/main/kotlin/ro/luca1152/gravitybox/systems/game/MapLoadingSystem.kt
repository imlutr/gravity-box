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
import ro.luca1152.gravitybox.utils.assets.Text
import ro.luca1152.gravitybox.utils.json.MapFactory
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Loads game maps from files. */
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
        val jsonData = manager.get<Text>("maps/game/map-${levelEntity.level.levelId}.json").string
        val mapFactory = Json().fromJson(MapFactory::class.java, jsonData)
        levelEntity.run {
            level.loadMap = false
            map.loadMap(mapFactory, playerEntity, finishEntity)
        }
    }
}