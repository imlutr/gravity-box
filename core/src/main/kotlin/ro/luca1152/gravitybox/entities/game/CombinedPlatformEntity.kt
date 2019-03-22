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
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.game.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object CombinedPlatformEntity {
    fun createEntity(
        isCombinedHorizontally: Boolean = false,
        isCombinedVertically: Boolean = false,
        engine: PooledEngine = Injekt.get()
    ) = engine.createEntity().apply {
        add(engine.createComponent(BodyComponent::class.java))
        add(engine.createComponent(CombinedBodyComponent::class.java)).run {
            combinedBody.set(this, isCombinedHorizontally, isCombinedVertically, entityContainsBody = true)
        }
        add(engine.createComponent(PlatformComponent::class.java))
        add(engine.createComponent(EditorObjectComponent::class.java))
        add(engine.createComponent(PolygonComponent::class.java))
        engine.addEntity(this)
    }!!
}