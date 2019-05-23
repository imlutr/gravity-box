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
import ro.luca1152.gravitybox.components.game.ColorComponent
import ro.luca1152.gravitybox.components.game.ColorType
import ro.luca1152.gravitybox.components.game.Scene2DComponent
import ro.luca1152.gravitybox.components.game.color
import ro.luca1152.gravitybox.utils.kotlin.setWithoutAlpha
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import ro.luca1152.gravitybox.utils.ui.Colors

/** Syncs the [Scene2DComponent]'s color with the color scheme. */
class ColorSyncSystem : IteratingSystem(Family.all(Scene2DComponent::class.java, ColorComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.syncColorsWithColorScheme()
    }

    private fun Entity.syncColorsWithColorScheme() {
        tryGet(Scene2DComponent)?.group?.children?.forEach {
            it.color.setWithoutAlpha(
                when (color.colorType) {
                    ColorType.LIGHT -> Colors.bgColor
                    ColorType.DARK -> Colors.gameColor
                    ColorType.DARKER_DARK -> Colors.uiDownColor
                    else -> throw IllegalArgumentException("Unrecognized colorType. Maybe not initialized?")
                }
            )
        }
    }
}