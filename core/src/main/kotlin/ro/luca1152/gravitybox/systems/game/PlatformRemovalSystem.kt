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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import ro.luca1152.gravitybox.components.game.BodyComponent
import ro.luca1152.gravitybox.components.game.DestroyablePlatformComponent
import ro.luca1152.gravitybox.components.game.destroyablePlatform
import ro.luca1152.gravitybox.components.game.scene2D

/** Removes every platform marked for removal. */
class PlatformRemovalSystem :
    IteratingSystem(Family.all(DestroyablePlatformComponent::class.java, BodyComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.run {
            if (destroyablePlatform.remove) {
                remove(BodyComponent::class.java)
                scene2D.isVisible = false
                destroyablePlatform.remove = false
            }
        }
    }
}