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
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.graphics.Color
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.player
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.ui.Colors

/** Gradually changes the color scheme when the player enters/leaves the finish point. */
class FinishPointColorSystem : IntervalSystem(1 / 70f) {
    companion object {
        private const val LERP_PROGRESS = .05f
    }

    private lateinit var playerEntity: Entity
    private lateinit var levelEntity: Entity

    private val targetGameColor: Color
        get() = when (playerEntity.player.isInsideFinishPoint && levelEntity.level.canFinish) {
            true -> when (Colors.useDarkTheme) {
                true -> Colors.LightTheme.game57
                false -> Colors.DarkTheme.game95
            }
            false -> when (Colors.useDarkTheme) {
                true -> Colors.DarkTheme.game95
                false -> Colors.LightTheme.game57
            }
        }

    private val targetBgColor: Color
        get() = when (playerEntity.player.isInsideFinishPoint && levelEntity.level.canFinish) {
            true -> when (Colors.useDarkTheme) {
                true -> Colors.LightTheme.game91
                false -> Colors.DarkTheme.game20
            }
            false -> when (Colors.useDarkTheme) {
                true -> Colors.DarkTheme.game20
                false -> Colors.LightTheme.game91
            }
        }

    override fun addedToEngine(engine: Engine) {
        playerEntity = engine.getSingleton<PlayerComponent>()
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun updateInterval() {
        if (Colors.isDirty) return
        lerpColors()
    }

    private fun lerpColors() {
        Colors.bgColor.lerp(targetBgColor, LERP_PROGRESS)
        Colors.gameColor.lerp(targetGameColor, LERP_PROGRESS)
    }
}