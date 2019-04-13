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

package ro.luca1152.gravitybox.systems.editor

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import ro.luca1152.gravitybox.components.editor.RotatingIndicatorComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.editor.rotatingIndicator
import ro.luca1152.gravitybox.components.game.scene2D
import ro.luca1152.gravitybox.systems.game.PhysicsSystem
import ro.luca1152.gravitybox.utils.ui.Colors

/** Sets the correct position and rotation of rotating indicators. */
class RotatingIndicatorSystem : IteratingSystem(Family.all(RotatingIndicatorComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.run {
            rotatingIndicator.indicatorImage.run {
                x = scene2D.centerX - width / 2f
                y = scene2D.centerY - height / 2f
                rotation = scene2D.rotation
                color = if (editorObject.isSelected) Colors.uiDownColor else Colors.gameColor
                isVisible = engine.getSystem(PhysicsSystem::class.java) == null
            }
        }
    }
}