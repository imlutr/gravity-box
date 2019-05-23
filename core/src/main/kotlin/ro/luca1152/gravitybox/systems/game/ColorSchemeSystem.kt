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
import ro.luca1152.gravitybox.components.game.FinishComponent
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.PlayerComponent
import ro.luca1152.gravitybox.utils.kotlin.approxEqualTo
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.ui.Colors

/** Updates the color scheme when the hue changes. */
class ColorSchemeSystem : EntitySystem() {
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity
    private lateinit var levelEntity: Entity
    private var generatedNewColors = false

    override fun addedToEngine(engine: Engine) {
        playerEntity = engine.getSingleton<PlayerComponent>()
        finishEntity = engine.getSingleton<FinishComponent>()
        levelEntity = engine.getSingleton<LevelComponent>()
    }

    override fun update(deltaTime: Float) {
        if (!Colors.isDirty) return
        lerpToNewHue()
    }

    private fun lerpToNewHue() {
        updateNewColors()
        lerpColors()
    }

    private fun updateNewColors() {
        if (generatedNewColors) return
        Colors.LightTheme.resetAllColors(Colors.hue)
        Colors.DarkTheme.resetAllColors(Colors.hue)
        generatedNewColors = true
    }

    private fun lerpColors() {
        Colors.lerpTowardsDefaultColors(.05f)
        if (Colors.uiDownColor.approxEqualTo(Colors.LightTheme.game29)) {
            Colors.isDirty = false
            generatedNewColors = false
        }
    }
}