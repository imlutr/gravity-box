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
        const val TOTAL_LEVELS = 10
        const val PPM = 32f // Pixels per meter
        lateinit var font32: BitmapFont
    }

    override fun create() {
        Box2D.init()

        // Add singletons to Injekt
        Injekt.addSingleton(SpriteBatch() as Batch)
        Injekt.addSingleton(AssetManager())
        Injekt.addSingleton(this as Game)
        Injekt.addSingleton(LoadingScreen())
        Injekt.addSingleton(PlayScreen())

        font32 = BitmapFont(Gdx.files.internal("fonts/font-32.fnt"))
        setScreen(Injekt.get<LoadingScreen>())
    }

    override fun dispose() {
        Injekt.get<Batch>().dispose()
        Injekt.get<AssetManager>().dispose()
        Injekt.get<LoadingScreen>().dispose()
        Injekt.get<PlayScreen>().dispose()
    }
}
