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

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.getAsset
import ktx.assets.load
import ktx.log.info
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme.lightColor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class LoadingScreen(private val manager: AssetManager = Injekt.get()) : KtxScreen {
    private var timer = 0f

    override fun show() {
        loadFont()
        loadGraphics()
        loadAudio()
        loadMaps()
    }

    private fun loadFont() {
        manager.run {
            load<Texture>("font/font.png", TextureLoader.TextureParameter().apply { genMipMaps = true })
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
            for (i in 1..10)
                load<TiledMap>("maps/map-$i.tmx")
        }
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(lightColor.r, lightColor.g, lightColor.b)
    }

    private fun update(delta: Float) {
        timer += delta
        // Finished loading assets
        if (manager.update()) {
            smoothTextures()
            info { "Finished loading assets in ${(timer * 100).toInt() / 100f}s." }
            Injekt.get<MyGame>().setScreen<PlayScreen>()
        }
    }

    private fun smoothTextures() {
        manager.run {
            getAsset<Texture>("graphics/player.png").setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            getAsset<Texture>("graphics/bullet.png").setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            getAsset<Texture>("graphics/circle.png").setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            getAsset<Texture>("graphics/finish.png").setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
    }
}