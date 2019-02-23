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

object PlayerEntity {
    private const val WIDTH = 1f
    private const val HEIGHT = 1f

    fun createEntity(id: Int, x: Float, y: Float,
                     manager: AssetManager = Injekt.get(),
                     engine: PooledEngine = Injekt.get()) = engine.createEntity().apply {
        add(engine.createComponent(IdComponent::class.java)).run {
            this.id.set(id)
        }
        add(engine.createComponent(PlayerComponent::class.java))
        add(engine.createComponent(ImageComponent::class.java)).run {
            image.set(manager.get<Texture>("graphics/player.png"), x, y, WIDTH, HEIGHT)
            image.img.userObject = this
        }
        add(engine.createComponent(ColorComponent::class.java)).run {
            color.set(ColorType.DARK)
        }
        engine.addEntity(this)
    }
}