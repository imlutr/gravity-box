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

package ro.luca1152.gravitybox

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Box2D

import ro.luca1152.gravitybox.screens.LoadingScreen
import ro.luca1152.gravitybox.screens.PlayScreen

class MyGame : Game() {
    // Constants
    enum class EntityCategory(bits: Int) {
        NONE(0x0000),
        FINISH(0x0001),
        PLAYER(0x0002),
        OBSTACLE(0x0003),
        BULLET(0x0004);

        var bits: Short = 0

        init {
            this.bits = bits.toShort()
        }
    }


    override fun create() {
        // Game
        MyGame.instance = this
        Box2D.init()

        // Tools
        MyGame.batch = SpriteBatch()
        MyGame.manager = AssetManager()

        // Screens
        MyGame.loadingScreen = LoadingScreen()
        MyGame.playScreen = PlayScreen()

        // Fonts
        font32 = BitmapFont(Gdx.files.internal("fonts/font-32.fnt"))
        preferences = Gdx.app.getPreferences("GMTK 2018 by Luca1152")

        setScreen(MyGame.loadingScreen)
    }

    override fun dispose() {
        MyGame.batch.dispose()
        MyGame.manager.dispose()
    }

    companion object {

        val TOTAL_LEVELS = 10f
        val PPM = 32f // Pixels per meter

        // Colors
        var lightColor = Color()
        var darkColor = Color()
        var lightColor2 = Color()
        var darkColor2 = Color()

        // Game
        lateinit var instance: MyGame

        // Tools
        lateinit var batch: Batch
        lateinit var manager: AssetManager
        lateinit var preferences: Preferences

        // Screens
        lateinit var playScreen: PlayScreen
        lateinit var loadingScreen: LoadingScreen

        // Fonts
        lateinit var font32: BitmapFont

        fun getLightColor(hue: Int): Color {
            val color = Color().fromHsv(hue.toFloat(), 10f / 100f, 91f / 100f)
            color.a = 1f
            return color
        }

        fun getDarkColor(hue: Int): Color {
            val color = Color().fromHsv(hue.toFloat(), 42f / 100f, 57f / 100f)
            color.a = 1f
            return color
        }

        fun getLightColor2(hue: Int): Color {
            val color = Color().fromHsv(hue.toFloat(), 94f / 100f, 20f / 100f)
            color.a = 1f
            return color
        }

        fun getDarkColor2(hue: Int): Color {
            val color = Color().fromHsv(hue.toFloat(), 85f / 100f, 95f / 100f)
            color.a = 1f
            return color
        }
    }
}
