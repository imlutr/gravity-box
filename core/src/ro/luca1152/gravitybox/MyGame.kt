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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Box2D
import ro.luca1152.gravitybox.screens.LoadingScreen
import ro.luca1152.gravitybox.screens.PlayScreen
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

class MyGame : Game() {
    companion object {
        lateinit var font32: BitmapFont
    }

    override fun create() {
        // Initialize Box2D
        Box2D.init()

        // Initialize dependency injection
        Injekt.run {
            addSingleton(SpriteBatch() as Batch)
            addSingleton(AssetManager())
            addSingleton(this@MyGame as Game)
            addSingleton(LoadingScreen())
            addSingleton(PlayScreen())
        }

        font32 = BitmapFont(Gdx.files.internal("fonts/font-32.fnt"))
        // Change the screen to the LoadingScreen
        setScreen(Injekt.get<LoadingScreen>())
    }

    override fun dispose() {
        Injekt.run {
            get<Batch>().dispose()
            get<AssetManager>().dispose()
            get<LoadingScreen>().dispose()
            get<PlayScreen>().dispose()
        }
    }
}
