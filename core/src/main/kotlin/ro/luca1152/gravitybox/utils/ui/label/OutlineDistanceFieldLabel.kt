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

package ro.luca1152.gravitybox.utils.ui.label

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.kotlin.OutlineDistanceFieldShader
import ro.luca1152.gravitybox.utils.ui.Colors

class OutlineDistanceFieldLabel(
    context: Context,
    text: CharSequence,
    skin: Skin,
    styleName: String,
    fontSize: Float = 32f,
    color: Color = Color.WHITE
) : BaseDistanceFieldLabel(text, skin, styleName, fontSize, color) {
    private val outlineDistanceFieldShader: OutlineDistanceFieldShader = context.inject()

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.shader = outlineDistanceFieldShader
        if (syncColorsWithColorScheme) {
            outlineDistanceFieldShader.setUniformf("u_outlineColor", Colors.bgColor)
        }
        super.draw(batch, parentAlpha)
        batch.shader = null
    }
}