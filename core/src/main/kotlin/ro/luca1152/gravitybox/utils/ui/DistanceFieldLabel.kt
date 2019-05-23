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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Align
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.kotlin.DistanceFieldShader
import ro.luca1152.gravitybox.utils.kotlin.setWithoutAlpha

class DistanceFieldLabel(
    context: Context,
    text: CharSequence,
    skin: Skin,
    styleName: String,
    fontSize: Float = 32f,
    color: Color = Color.WHITE
) : Label(text, skin, styleName, Color.WHITE) {
    companion object {
        val vertexShader = """
            uniform mat4 u_projTrans;

            attribute vec4 a_position;
            attribute vec2 a_texCoord0;
            attribute vec4 a_color;

            varying vec4 v_color;
            varying vec2 v_texCoord;

            void main() {
                gl_Position = u_projTrans * a_position;
                v_texCoord = a_texCoord0;
                v_color = a_color;
            }
        """.trimIndent()

        val fragmentShader = """
            #ifdef GL_ES
            precision mediump float;
            #endif

            uniform sampler2D u_texture;

            varying vec4 v_color;
            varying vec2 v_texCoord;

            const float smoothing = 1.0/16.0;

            void main() {
                float distance = texture2D(u_texture, v_texCoord).a;
                float alpha = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
                gl_FragColor = vec4(v_color.rgb, v_color.a * alpha);
            }
        """.trimIndent()

        private const val DEFAULT_FONT_SIZE = 32f
    }

    private var syncColorsWithColorScheme = true
    private val distanceFieldShader: DistanceFieldShader = context.inject()

    init {
        this.color = color
        setFontScale(fontSize / DEFAULT_FONT_SIZE)
        if (this.color != Colors.gameColor && this.color != Colors.bgColor) {
            syncColorsWithColorScheme = false
        }
        setAlignment(Align.center, Align.center)
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (syncColorsWithColorScheme && color != Colors.uiDownColor) {
            color.setWithoutAlpha(Colors.gameColor)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.shader = distanceFieldShader
        super.draw(batch, parentAlpha)
        batch.shader = null
    }
}