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
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.physics.box2d.BodyDef
import ro.luca1152.gravitybox.components.*
import ro.luca1152.gravitybox.utils.box2d.EntityCategory
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object FinishEntity {
    const val WIDTH = 2f
    const val HEIGHT = 2f
    val CATEGORY_BITS = EntityCategory.FINISH.bits
    val MASK_BITS = EntityCategory.FINISH.bits

    fun createEntity(id: Int, x: Float, y: Float,
                     blinkEndlessly: Boolean = true,
                     manager: AssetManager = Injekt.get(),
                     engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        add(engine.createComponent(NewMapObjectComponent::class.java)).run {
            newMapObject.set(id)
        }
        add(engine.createComponent(ImageComponent::class.java)).run {
            image.set(manager.get<Texture>("graphics/finish.png"), x, y, WIDTH, HEIGHT)
            image.img.userObject = this
        }
        add(engine.createComponent(FinishComponent::class.java)).run {
            finish.set(blinkEndlessly, image)
        }
        add(engine.createComponent(BodyComponent::class.java)).run {
            body.set(image.imageToBox2DBody(BodyDef.BodyType.StaticBody, CATEGORY_BITS, MASK_BITS), this, CATEGORY_BITS, MASK_BITS)
        }
        add(engine.createComponent(CollisionBoxComponent::class.java)).run {
            collisionBox.set(WIDTH, HEIGHT)
        }
        add(engine.createComponent(ColorComponent::class.java)).run {
            color.set(ColorType.DARK)
        }
        add(engine.createComponent(MapObjectOverlayComponent::class.java)).run {
            mapObjectOverlay.set(showMovementButtons = true, showRotationButton = true, showResizingButtons = false, showDeletionButton = false)
        }
        add(engine.createComponent(JsonComponent::class.java)).run {
            json.setObject(this, "finish")
        }
        engine.addEntity(this)
    }!!
}