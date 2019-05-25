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
import com.badlogic.gdx.assets.AssetManager
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.ui.Colors

/** Loads game maps from files. */
class MapLoadingSystem(private val context: Context) : EntitySystem() {
    private val manager: AssetManager = context.inject()

    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity

    private var loadedAnyMap = false

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>()
        playerEntity = engine.getSingleton<PlayerComponent>()
        finishEntity = engine.getSingleton<FinishComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!levelEntity.level.loadMap)
            return
        loadMap()
    }

    private fun loadMap() {
        levelEntity.run {
            level.loadMap = false
            map.loadMap(
                context, manager.get(Assets.gameMaps).mapPackFactory.maps[levelEntity.level.levelId - 1],
                playerEntity, finishEntity
            )
        }
        initializeColorScheme()
    }

    private fun initializeColorScheme() {
        // Only when the game first starts, after the splash screen
        if (!loadedAnyMap) {
            Colors.updateAllColors()
            loadedAnyMap = true
        }
    }
}