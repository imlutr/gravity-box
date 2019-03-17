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
import ro.luca1152.gravitybox.components.editor.ExtendedTouchComponent
import ro.luca1152.gravitybox.components.editor.extendedTouch
import ro.luca1152.gravitybox.components.game.ImageComponent
import ro.luca1152.gravitybox.components.game.image

/** Syncs the [ExtendedTouchComponent]'s position and size with [ImageComponent]'s.*/
class ExtendedTouchSyncSystem :
    IteratingSystem(Family.all(ImageComponent::class.java, ExtendedTouchComponent::class.java).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.extendedTouch.run {
            setPosition(entity.image.centerX, entity.image.centerY)
            setSize(entity.image.width, entity.image.height)
            boundsImage.rotation = entity.image.img.rotation
        }
    }
}