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

package ro.luca1152.gravitybox.entities

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image

import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme.darkColor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

internal class Explosion(x: Float, y: Float,
                         manager: AssetManager = Injekt.get()) : Image(manager.get("graphics/circle.png", Texture::class.java)) {
    init {
        setSize(128 / MyGame.PPM, 128 / MyGame.PPM)
        setOrigin(width / 2f, height / 2f)
        setPosition(x - width / 2f, y - height / 2f)
        color = darkColor
        setScale(1 / 6f)
        addAction(Actions.sequence(
                Actions.parallel(
                        Actions.scaleBy(1f, 1f, .35f),
                        Actions.fadeOut(.35f, Interpolation.exp5)
                ),
                Actions.removeActor()
        ))
    }
}
