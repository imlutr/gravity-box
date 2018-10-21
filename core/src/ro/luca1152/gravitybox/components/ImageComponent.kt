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

package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.actors.plus
import ro.luca1152.gravitybox.pixelsToMeters

class ImageComponent(stage: Stage, texture: Texture, x: Float, y: Float) : Component {
    val image: Image = Image(texture)

    init {
        image.apply {
            setPosition(x, y)
            setSize(texture.width.pixelsToMeters, texture.height.pixelsToMeters)
            setOrigin(width / 2f, height / 2f)
        }
        stage + image
    }

    companion object : ComponentResolver<ImageComponent>(ImageComponent::class.java)
}

val Entity.image: Image
    get() = ImageComponent[this].image