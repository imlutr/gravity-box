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
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.graphics.Color
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.components.game.player
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.ui.ColorScheme

/** Gradually changes the color scheme when the player enters/leaves the finish point. */
class FinishPointColorSystem : IntervalSystem(1 / 70f) {
    companion object {
        private const val LERP_PROGRESS = .05f
    }

    private lateinit var playerEntity: Entity
    private val targetDarkColor: Color
        get() = if (playerEntity.player.isInsideFinishPoint) ColorScheme.darkColor2 else ColorScheme.darkColor
    private val targetLightColor: Color
        get() = if (playerEntity.player.isInsideFinishPoint) ColorScheme.lightColor2 else ColorScheme.lightColor

    override fun addedToEngine(engine: Engine) {
        playerEntity = engine.getSingletonFor(Family.all(PlayerComponent::class.java).get())
    }

    override fun updateInterval() {
        lerpColors()
    }

    private fun lerpColors() {
        ColorScheme.currentDarkColor.lerp(targetDarkColor, LERP_PROGRESS)
        ColorScheme.currentLightColor.lerp(targetLightColor, LERP_PROGRESS)
    }
}