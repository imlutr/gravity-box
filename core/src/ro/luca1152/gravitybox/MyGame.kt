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

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxGame
import ro.luca1152.gravitybox.screens.LoadingScreen
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.GameCamera
import ro.luca1152.gravitybox.utils.GameViewport
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

/**
 * The main class of the game.
 */
class MyGame : KtxGame<Screen>() {
    override fun create() {
        // Initialize Injekt, the dependency injection library
        Injekt.run {
            addSingleton(this@MyGame)
            addSingleton(SpriteBatch() as Batch)
            addSingleton(ShapeRenderer())
            addSingleton(AssetManager())
            addSingleton(GameCamera)
            addSingleton(GameViewport)
            addSingleton(PooledEngine())
        }

        // Add the screens so setScreen<[Screen]>() can be used
        addScreen(PlayScreen()); addScreen(LoadingScreen())

        setScreen<LoadingScreen>()
    }

    override fun dispose() {
        // Dispose every screen
        super.dispose()

        // Dispose heavy objects
        Injekt.run {
            get<Batch>().dispose()
            get<AssetManager>().dispose()
            get<ShapeRenderer>().dispose()
            get<World>().dispose()
        }
    }
}

/** Pixels per meter. */
const val PPM = 64f

val Int.pixelsToMeters: Float
    get() = this / PPM

val Float.pixelsToMeters: Float
    get() = this / PPM