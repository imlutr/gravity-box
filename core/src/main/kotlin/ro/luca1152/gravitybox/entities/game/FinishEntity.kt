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

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType.StaticBody
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.editor.json
import ro.luca1152.gravitybox.components.editor.overlay
import ro.luca1152.gravitybox.components.editor.snap
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object FinishEntity {
    const val WIDTH = 2f
    const val HEIGHT = 2f
    /** The finish point's color alpha used when the level cannot be finished. */
    const val FINISH_BLOCKED_ALPHA = .2f
    val CATEGORY_BITS = EntityCategory.FINISH.bits
    val MASK_BITS = EntityCategory.FINISH.bits

    fun createEntity(
        id: Int = 0, x: Float = 0f, y: Float = 0f,
        blinkEndlessly: Boolean = true,
        manager: AssetManager = Injekt.get()
    ) = newEntity().apply {
        mapObject(id)
        scene2D()
        scene2D(manager.get(Assets.tileset).findRegion("finish"), x, y, WIDTH, HEIGHT)
        polygon(scene2D)
        editorObject()
        snap()
        finish()
        if (blinkEndlessly) {
            // Delay the effect so it starts after transitioning to the PlayScreen
            fadeInFadeOut(scene2D, .25f)
        }
        body(scene2D.toBody(StaticBody, CATEGORY_BITS, MASK_BITS), CATEGORY_BITS, MASK_BITS)
        collisionBox(WIDTH, HEIGHT)
        color(ColorType.DARK)
        overlay(
            showMovementButtons = true, showRotationButton = true,
            showResizingButtons = false, showDeletionButton = false
        )
        json(this, "finish")
        addToEngine()
    }
}