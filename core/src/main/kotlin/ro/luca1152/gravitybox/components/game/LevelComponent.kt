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

package ro.luca1152.gravitybox.components.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.utils.kotlin.createComponent

/** Contains level information. */
class LevelComponent : Component, Poolable {
    var loadMap = false
    var createCombinedBodies = false
    var forceUpdateMap = false
    var restartLevel = false
    var isRestarting = false
    var isChangingLevel = false
    var levelId = 1

    /** True if every point (if any) was collected. */
    var canFinish = true

    fun set(levelNumber: Int) {
        this.levelId = levelNumber
    }

    override fun reset() {
        loadMap = false
        createCombinedBodies = false
        forceUpdateMap = false
        restartLevel = false
        isRestarting = false
        isChangingLevel = false
        levelId = 1
    }

    companion object : ComponentResolver<LevelComponent>(LevelComponent::class.java)
}

val Entity.level: LevelComponent
    get() = LevelComponent[this]

fun Entity.level(context: Context, levelNumber: Int) =
    add(createComponent<LevelComponent>(context).apply {
        set(levelNumber)
    })!!