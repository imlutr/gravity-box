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
import ro.luca1152.gravitybox.components.LevelComponent
import ro.luca1152.gravitybox.components.PlayerComponent
import ro.luca1152.gravitybox.components.level
import ro.luca1152.gravitybox.components.player
import ro.luca1152.gravitybox.utils.kotlin.approxEqualTo
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.ui.ColorScheme

/** Handles what happens when a level is finished. */
class LevelFinishSystem(private val restartLevelWhenFinished: Boolean = false) : EntitySystem() {
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity

    // The color scheme is the one that tells whether the level was finished: if the current color scheme
    // is the same as the dark color scheme, then it means that the level was finished. I should change
    // this in the future.
    private val colorSchemeIsFullyTransitioned
        get() = ColorScheme.currentDarkColor.approxEqualTo(ColorScheme.darkColor2)
    private val levelIsFinished
        get() = playerEntity.player.isInsideFinishPoint && colorSchemeIsFullyTransitioned

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get())
        playerEntity = engine.getSingletonFor(Family.all(PlayerComponent::class.java).get())
    }

    override fun update(deltaTime: Float) {
        if (!levelIsFinished)
            return
        handleLevelFinish()
    }

    private fun handleLevelFinish() {
        // Used in the level editor
        if (restartLevelWhenFinished)
            levelEntity.level.restartLevel = true
        else {
            // TODO
        }
    }

    override fun removedFromEngine(engine: Engine?) {
    }
}