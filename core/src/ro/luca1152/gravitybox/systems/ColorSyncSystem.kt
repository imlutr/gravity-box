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

package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.setWithoutAlpha

/**
 * Sync the [ImageComponent]'s color with the [ColorComponent]'s color.
 * Used mostly to set the color of every object to the respective color from ColorScheme.
 */
class ColorSyncSystem : IteratingSystem(Family.all(ImageComponent::class.java, ColorComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        when (entity.color.colorType) {
            ColorType.DARK -> entity.image.color.setWithoutAlpha(ColorScheme.currentDarkColor)
            ColorType.LIGHT -> entity.image.color.setWithoutAlpha(ColorScheme.currentLightColor)
        }
    }
}