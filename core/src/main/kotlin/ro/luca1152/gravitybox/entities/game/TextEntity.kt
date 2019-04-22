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

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ro.luca1152.gravitybox.components.editor.json
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.OverlayStage
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object TextEntity {
    fun createEntity(
        id: Int,
        string: String,
        x: Float, y: Float,
        skin: Skin = Injekt.get(),
        overlayStage: OverlayStage = Injekt.get()
    ) = newEntity().apply {
        mapObject(id)
        text(string)
        scene2D()
        scene2D.run {
            group.run {
                val label = DistanceFieldLabel(text.string, skin, "regular", 37f).apply {
                    setSize(prefWidth, prefHeight)
                }
                addActor(label)
                setSize(label.width, label.height)
            }
            centerX = x
            centerY = y
            overlayStage.addActor(group)
        }
        color(ColorType.DARK)
        json(this, "text")
        addToEngine()
    }
}