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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader

import ro.luca1152.gravitybox.MyGame

class LoadingScreen : ScreenAdapter() {
    private val TAG = LoadingScreen::class.java.simpleName
    private var timer = 0f

    override fun show() {
        Gdx.app.log(TAG, "Entered screen.")
        loadAssets()
    }

    private fun loadAssets() {
        // Textures
        MyGame.manager.load("graphics/player.png", Texture::class.java)
        MyGame.manager.load("graphics/bullet.png", Texture::class.java)
        MyGame.manager.load("graphics/circle.png", Texture::class.java)
        MyGame.manager.load("graphics/finish.png", Texture::class.java)

        // Audio
        MyGame.manager.load("audio/music.mp3", Music::class.java)
        MyGame.manager.load("audio/level-finished.wav", Sound::class.java)
        MyGame.manager.load("audio/bullet-wall-collision.wav", Sound::class.java)

        // Maps
        MyGame.manager.setLoader<TiledMap, TmxMapLoader.Parameters>(TiledMap::class.java, TmxMapLoader())
        var i = 1
        while (i <= MyGame.TOTAL_LEVELS) {
            MyGame.manager.load("maps/map-$i.tmx", TiledMap::class.java)
            i++
        }
    }

    override fun render(delta: Float) {
        update(delta)
        Gdx.gl20.glClearColor(209 / 255f, 232 / 255f, 232 / 255f, 1f)
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    private fun update(delta: Float) {
        timer += delta

        // Finished loading assets
        if (MyGame.manager.update()) {
            MyGame.manager.get("graphics/player.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            MyGame.manager.get("graphics/bullet.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            MyGame.manager.get("graphics/circle.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            MyGame.manager.get("graphics/finish.png", Texture::class.java).setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            timer = timer.toInt() * 100 / 100f
            Gdx.app.log(TAG, "Finished loading assets in " + timer + "s.")
            MyGame.instance.screen = MyGame.playScreen
        }
    }

    override fun hide() {
        Gdx.app.log(TAG, "Left screen.")
    }
}
