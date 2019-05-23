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
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.editor.extendedTouch
import ro.luca1152.gravitybox.components.editor.json
import ro.luca1152.gravitybox.components.editor.overlay
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel

object TextEntity {
    fun createEntity(
        context: Context,
        string: String,
        x: Float,
        y: Float
    ) = newEntity(context).apply {
        val skin: Skin = context.inject()
        val gameStage: GameStage = context.inject()
        mapObject(context)
        text(context, string)
        scene2D(context)
        scene2D.run {
            group.run {
                val label = DistanceFieldLabel(context, text.string, skin, "regular", 37f).apply {
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
        extendedTouch(context, this, scene2D.width, scene2D.height)

        polygon(context, scene2D)
        editorObject(context)
        overlay(
            context,
            showMovementButtons = true, showRotationButton = false,
            showDeletionButton = true, showResizingButtons = false, showSettingsButton = false
        )
        color(context, ColorType.DARK)
        json(context, this)
        addToEngine(context)
    }
}