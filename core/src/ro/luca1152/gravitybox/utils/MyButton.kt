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

package ro.luca1152.gravitybox.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener

class MyButton(skin: Skin, styleName: String, drawableName: String = "",
               upColor: Color = Color.WHITE, downColor: Color = Color.WHITE,
               touchUpRunnable: Runnable? = null,
               private val isTransparent: Boolean = true) : Button(skin, styleName) {
    private val colorSwapVert = """
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;

        uniform mat4 u_projTrans;

        varying vec4 v_color;
        varying vec2 v_texCoords;

        void main(){
            v_color = a_color;
            v_color.a = v_color.a * (255.0/254.0);
            v_texCoords = a_texCoord0;
            gl_Position =  u_projTrans * a_position;
        }
    """.trimIndent()

    private val colorSwapFrag = """
        #ifdef GL_ES
            precision mediump float;
        #endif

        varying vec4 v_color;
        varying vec2 v_texCoords;

        uniform sampler2D u_texture;
        uniform sampler2D u_colorTable;

        void main()
        {
            vec4 color = texture2D(u_texture, v_texCoords);
            vec4 swapColor = texture2D(u_colorTable, vec2(color.r, 0.0));
            vec4 finalColor = mix(v_color * color, swapColor, swapColor.a);
            finalColor.a = (v_color*color).a;
            gl_FragColor = finalColor;
//            gl_FragColor = vec4(swapColor.a);
//            gl_FragColor = color;
        }
    """.trimIndent()

    val image = if (drawableName != "") Image(skin, drawableName) else null
    var imageCell: Cell<Image>? = null
    private var colorSwapShader: ShaderProgram = ShaderProgram(colorSwapVert, colorSwapFrag)
    private var colorTable: Texture? = null

    init {
        if (image != null)
            imageCell = add(image)

        if (!isTransparent) {
            println(colorSwapShader.log)
            val colorTablePixmap = Pixmap(256, 1, Pixmap.Format.RGBA8888)
            colorTablePixmap.setColor(ColorScheme.currentLightColor)
            colorTablePixmap.fillRectangle(254, 0, 1, 1)
            colorTable = Texture(colorTablePixmap)
            ShaderProgram.pedantic = false
        }

        color = upColor
        image?.color = upColor

        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                color = downColor
                image?.color = downColor
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                color = upColor
                image?.color = upColor

                if (isOver(this@MyButton, x, y))
                    touchUpRunnable?.run()
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!isTransparent) {
            batch.shader = colorSwapShader
            colorTable?.bind(1)
            colorSwapShader.setUniformi("u_colorTable", 1)
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)
            super.draw(batch, parentAlpha)
            batch.shader = null
        } else {
            super.draw(batch, parentAlpha)
        }

    }
}