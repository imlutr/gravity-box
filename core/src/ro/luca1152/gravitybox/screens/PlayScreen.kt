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

import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.entities.Level

class PlayScreen : ScreenAdapter() {
    private val TAG = PlayScreen::class.java.simpleName
    private var level: Level? = null
    private var levelNumber = 1

    override fun show() {
        Gdx.app.log(TAG, "Entered screen.")
        val music = MyGame.manager.get("audio/music.mp3", Music::class.java)
        music.volume = .30f
        music.isLooping = true
        music.play()
        level = Level(levelNumber)
    }

    override fun render(delta: Float) {
        update(delta)
        Gdx.gl20.glClearColor(MyGame.lightColor.r, MyGame.lightColor.g, MyGame.lightColor.b, MyGame.lightColor.a)
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)
        level!!.draw()
    }

    private fun update(delta: Float) {
        timer += delta
        level!!.update(delta)
        if (level!!.reset) {
            level = Level(levelNumber)
            level!!.reset = false
        }
        if (level!!.isFinished && levelNumber + 1 <= MyGame.TOTAL_LEVELS) {
            level = Level(++levelNumber)
            MyGame.manager.get("audio/level-finished.wav", Sound::class.java).play(.2f)
        }
    }

    override fun hide() {
        Gdx.app.log(TAG, "Left screen.")
    }

    override fun resize(width: Int, height: Int) {
        level!!.stage.viewport.update(width, height)
    }

    companion object {
        var timer = 0f
    }
}
