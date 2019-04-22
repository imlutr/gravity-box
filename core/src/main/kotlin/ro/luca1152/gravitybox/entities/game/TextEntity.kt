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
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.GameStage
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
        gameStage: GameStage = Injekt.get()
    ) = newEntity().apply {
        mapObject(id)
        text(string)
        scene2D()
        scene2D.run {
            group.run {
                val label = DistanceFieldLabel(text.string, skin, "regular", 37f).apply {
                    setSize(prefWidth, prefHeight)
                }
                setScale(1 / PPM)
                addActor(label)
                setSize(label.width.pixelsToMeters, label.height.pixelsToMeters)
            }
            centerX = x.pixelsToMeters
            centerY = y.pixelsToMeters
            gameStage.addActor(group)
        }
        // Can't get the scene2D size to work properly (it is too small), so this is a workaround, as there wouldn't
        // be a a need for extended touch, normally
        extendedTouch(this, scene2D.width, scene2D.height)

        polygon(scene2D)
        snap()
        editorObject()
        overlay(
            showMovementButtons = true, showRotationButton = false,
            showDeletionButton = true, showResizingButtons = false, showSettingsButton = false
        )
        color(ColorType.DARK)
        json(this)
        addToEngine()
    }
}