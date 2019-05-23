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
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import ktx.app.KtxInputAdapter
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.utils.kotlin.getSingleton

/** Marks the level as to be restarted when the key 'R' is pressed. */
class KeyboardLevelRestartSystem(context: Context) : EntitySystem() {
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private lateinit var playerEntity: Entity
    private lateinit var levelEntity: Entity
    private val inputAdapter = object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.R) {
                if (!levelEntity.level.isRestarting) {
                    levelEntity.level.restartLevel = true
                }
                return true
            }
            return false
        }
    }

    override fun addedToEngine(engine: Engine) {
        playerEntity = engine.getSingleton<PlayerComponent>()
        levelEntity = engine.getSingleton<LevelComponent>()
        inputMultiplexer.addProcessor(inputAdapter)
    }

    override fun removedFromEngine(engine: Engine) {
        inputMultiplexer.removeProcessor(inputAdapter)
    }
}