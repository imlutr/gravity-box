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

@file:Suppress("LibGDXStaticResource")

package ro.luca1152.gravitybox.utils.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class DistanceFieldLabel(
    text: CharSequence,
    skin: Skin,
    styleName: String
) : Label(text, skin, styleName) {
    companion object {
        private val distanceFieldShader = DistanceFieldFont.createDistanceFieldShader()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val oldShader = batch.shader
        batch.shader = distanceFieldShader
        super.draw(batch, parentAlpha)
        batch.shader = oldShader
    }
}