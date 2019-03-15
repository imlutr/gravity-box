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

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.BodyDef
import ro.luca1152.gravitybox.components.editor.JsonComponent
import ro.luca1152.gravitybox.components.editor.MapObjectOverlayComponent
import ro.luca1152.gravitybox.components.editor.json
import ro.luca1152.gravitybox.components.editor.mapObjectOverlay
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.screens.Assets
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
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
        manager: AssetManager = Injekt.get(),
        engine: PooledEngine = Injekt.get()
    ) = engine.createEntity().apply {
        add(engine.createComponent(MapObjectComponent::class.java)).run {
            this.mapObject.set(id)
        }
        add(engine.createComponent(MapObjectOverlayComponent::class.java)).run {
            mapObjectOverlay.set(
                showMovementButtons = true,
                showRotationButton = true,
                showResizingButtons = false,
                showDeletionButton = false
            )
        }
        add(engine.createComponent(PlayerComponent::class.java))
        add(engine.createComponent(ImageComponent::class.java)).run {
            image.set(manager.get(Assets.tileset).findRegion("player"), x, y, WIDTH, HEIGHT)
            image.img.userObject = this
        }
        add(engine.createComponent(BodyComponent::class.java)).run {
            body.set(
                image.imageToBox2DBody(BodyDef.BodyType.DynamicBody, CATEGORY_BITS, MASK_BITS, DENSITY, FRICTION),
                this, CATEGORY_BITS, MASK_BITS, DENSITY, FRICTION
            )
        }
        add(engine.createComponent(CollisionBoxComponent::class.java)).run {
            collisionBox.set(WIDTH, HEIGHT)
        }
        add(engine.createComponent(ColorComponent::class.java)).run {
            color.set(ColorType.DARK)
        }
        add(engine.createComponent(JsonComponent::class.java)).run {
            json.setObject(this, "player")
        }
        engine.addEntity(this)
    }!!
}