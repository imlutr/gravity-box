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
import ro.luca1152.gravitybox.components.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

object PlatformEntity {
    private const val DEFAULT_WIDTH = 1f
    private const val DEFAULT_HEIGHT = .25f

    fun createEntity(id: Int, x: Float, y: Float,
                     engine: PooledEngine = Injekt.get(),
                     manager: AssetManager = Injekt.get()) = engine.createEntity().apply {
        add(engine.createComponent(NewMapObjectComponent::class.java)).run {
            newMapObject.set(id)
        }
        add(engine.createComponent(MapObjectOverlayComponent::class.java)).run {
            mapObjectOverlay.set(showMovementButtons = true, showRotationButton = true, showResizingButtons = true, showDeletionButton = true)
        }
        add(engine.createComponent(PlatformComponent::class.java))
        add(engine.createComponent(ImageComponent::class.java)).run {
            image.set(manager.get<Texture>("graphics/pixel.png"), x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT)
            image.img.userObject = this
        }
        add(engine.createComponent(TouchableBoundsComponent::class.java)).run {
            touchableBounds.set(this, 0f, 1f - DEFAULT_HEIGHT)
        }
        add(engine.createComponent(ColorComponent::class.java)).run {
            color.set(ColorType.DARK)
        }

        engine.addEntity(this)
    }!!
}