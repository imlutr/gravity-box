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

object PlayerEntity {
    const val WIDTH = 1f
    const val HEIGHT = 1f
    val CATEGORY_BITS = EntityCategory.PLAYER.bits
    val MASK_BITS = EntityCategory.OBSTACLE.bits
    const val FRICTION = 2f
    const val DENSITY = 1.15f

    fun createEntity(
        id: Int = 0, x: Float = 0f, y: Float = 0f,
        manager: AssetManager = Injekt.get()
    ) = newEntity().apply {
        mapObject(id)
        overlay(
            showMovementButtons = true, showRotationButton = true,
            showResizingButtons = false, showDeletionButton = false
        )
        player()
        image(manager.get(Assets.tileset).findRegion("player"), x, y, WIDTH, HEIGHT)
        image.img.userObject = this
        polygon(image.img)
        polygon.update()
        editorObject()
        snap()
        body()
        collisionBox(WIDTH, HEIGHT)
        color(ColorType.DARK)
        json(this, "player")
        addToEngine()
    }
}