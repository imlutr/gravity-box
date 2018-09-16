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

package ro.luca1152.gravitybox.screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.Texture.TextureFilter.MipMapLinearLinear
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.assets.getAsset
import ktx.assets.load
import ro.luca1152.gravitybox.entities.Level
import ro.luca1152.gravitybox.utils.ColorScheme.lightColor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get


var font = BitmapFont()
var fontShader = ShaderProgram(Gdx.files.internal("font/shader/font.vert"), Gdx.files.internal("font/shader/font.frag"))

class LoadingScreen(private val manager: AssetManager = Injekt.get()) : ScreenAdapter() {
    private var timer = 0f

    override fun show() {
        loadFont()
        loadGraphics()
        loadAudio()
        loadMaps()
    }

    private fun loadFont() {
        manager.run {
            load<Texture>("font/font.png", TextureParameter().apply { genMipMaps = true })
            load<BitmapFont>("font/font.fnt")
        }
    }

    private fun loadGraphics() {
        manager.run {
            load<Skin>("skin/skin.json")
            load<Texture>("graphics/player.png")
            load<Texture>("graphics/bullet.png")
            load<Texture>("graphics/bullet.png")
            load<Texture>("graphics/circle.png")
            load<Texture>("graphics/finish.png")
        }
    }

    private fun loadAudio() {
        manager.run {
            load<Music>("audio/music.mp3")
            load<Sound>("audio/level-finished.wav")
            load<Sound>("audio/bullet-wall-collision.wav")
        }
    }

    private fun loadMaps() {
        manager.run {
            setLoader<TiledMap, TmxMapLoader.Parameters>(TiledMap::class.java, TmxMapLoader())
            for (i in 1..Level.TOTAL_LEVELS)
                load<TiledMap>("maps/map-$i.tmx")
        }
    }

    override fun render(delta: Float) {
        update(delta)
        Gdx.gl20.glClearColor(lightColor.r, lightColor.g, lightColor.b, lightColor.a)
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    private fun update(delta: Float) {
        timer += delta

        // Finished loading assets
        if (manager.update()) {
            createFont()
            smoothTextures()
            logLoadingTime()

            // Change the screen to PlayScreen
            Injekt.get<Game>().screen = Injekt.get<PlayScreen>()
        }
    }

    private fun createFont() {
        val fontTexture = manager.getAsset<Texture>("font/font.png").apply { setFilter(MipMapLinearLinear, Linear) }
        font = BitmapFont(manager.getAsset<BitmapFont>("font/font.fnt").data.fontFile, TextureRegion(fontTexture))
    }

    private fun smoothTextures() {
        manager.run {
            getAsset<Texture>("graphics/player.png").setFilter(Linear, Linear)
            getAsset<Texture>("graphics/bullet.png").setFilter(Linear, Linear)
            getAsset<Texture>("graphics/circle.png").setFilter(Linear, Linear)
            getAsset<Texture>("graphics/finish.png").setFilter(Linear, Linear)
        }
    }

    private fun logLoadingTime() {
        timer = (timer * 100).toInt() / 100f
        Gdx.app.log(LoadingScreen::class.java.simpleName, "Finished loading assets in " + timer + "s.")
    }
}
