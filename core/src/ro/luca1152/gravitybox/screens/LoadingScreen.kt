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
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ro.luca1152.gravitybox.entities.Level
import ro.luca1152.gravitybox.utils.ColorScheme.lightColor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get


var font: BitmapFont = BitmapFont()
var fontShader: ShaderProgram = ShaderProgram(Gdx.files.internal("font/shader/font.vert"), Gdx.files.internal("font/shader/font.frag"))

class LoadingScreen(private val manager: AssetManager = Injekt.get()) : ScreenAdapter() {
    private var timer = 0f

    override fun show() {
        manager.load("skin/skin.json", Skin::class.java)
        loadFont()
        loadGraphics()
        loadAudio()
        loadMaps()
    }

    private fun loadGraphics() {
        manager.run {
            load("graphics/player.png", Texture::class.java)
            load("graphics/bullet.png", Texture::class.java)
            load("graphics/circle.png", Texture::class.java)
            load("graphics/finish.png", Texture::class.java)
        }
    }

    private fun loadAudio() {
        manager.run {
            load("audio/music.mp3", Music::class.java)
            load("audio/level-finished.wav", Sound::class.java)
            load("audio/bullet-wall-collision.wav", Sound::class.java)
        }
    }

    private fun loadFont() {
        manager.load("font/font.png", Texture::class.java)
        manager.load("font/font.fnt", BitmapFont::class.java)
    }

    private fun loadMaps() {
        manager.run {
            setLoader<TiledMap, TmxMapLoader.Parameters>(TiledMap::class.java, TmxMapLoader())
            for (i in 1..Level.TOTAL_LEVELS)
                load("maps/map-$i.tmx", TiledMap::class.java)
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
            smoothTextures()
            createFont()
            logLoadingTime()

            // Change the screen to PlayScreen
            Injekt.get<Game>().screen = Injekt.get<PlayScreen>()
        }
    }

    private fun smoothTextures() {
        manager.run {
            get("graphics/player.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            get("graphics/bullet.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            get("graphics/circle.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            get("graphics/finish.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }

    private fun createFont() {
        val fontTexture = Texture(Gdx.files.internal("font/font.png"), true).apply {
            setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.MipMapLinearLinear)
        }
        font = BitmapFont(manager.get("font/font.fnt", BitmapFont::class.java).data.fontFile, TextureRegion(fontTexture))
    }

    private fun logLoadingTime() {
        timer = (timer * 100).toInt() / 100f
        Gdx.app.log(LoadingScreen::class.java.simpleName, "Finished loading assets in " + timer + "s.")
    }
}
