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

package ro.luca1152.gravitybox.entities.game

import com.badlogic.ashley.core.PooledEngine
import ro.luca1152.gravitybox.components.game.LevelComponent
import ro.luca1152.gravitybox.components.game.MapComponent
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object LevelEntity {
    fun createEntity(
        levelId: Int = 0,
        engine: PooledEngine = Injekt.get()
    ) =
        engine.createEntity().apply {
            add(engine.createComponent(LevelComponent::class.java)).run {
                level.set(levelId)
            }
            add(engine.createComponent(MapComponent::class.java)).run {
                map.set(levelId)
            }
            engine.addEntity(this)
        }!!
}