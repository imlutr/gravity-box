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
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxGame
import ro.luca1152.gravitybox.screens.*
import ro.luca1152.gravitybox.utils.kotlin.GameCamera
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.GameViewport
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

/** The main class of the game. */
class MyGame : KtxGame<Screen>() {
    companion object {
        const val LEVELS_NUMBER = 4
    }

    override fun create() {
        // Load the Box2D native library
        Box2D.init()

        // Initialize Injekt, the dependency injection library
        Injekt.run {
            addSingleton(this@MyGame)
            addSingleton(SpriteBatch() as Batch)
            addSingleton(ShapeRenderer())
            addSingleton(AssetManager())
            addSingleton(GameCamera)
            addSingleton(GameViewport)
            addSingleton(GameStage)
            addSingleton(PooledEngine())
        }

        // Add screens so setScreen<[Screen]>() can be used
        addScreen(LoadingScreen())
        addScreen(MainMenuScreen())
        addScreen(LevelEditorScreen())
        addScreen(LevelSelectorScreen())
        addScreen(PlayScreen())

        // Randomize the color scheme every time the game starts
        ColorScheme.hue = MathUtils.random(0, 360).toFloat()

        // Generate the actual colors based on the hue
        ColorScheme.updateColors()

        // Go to the loading screen
        setScreen<LoadingScreen>()
    }

    override fun dispose() {
        // Dispose EVERY screen
        super.dispose()

        // Dispose heavy injected objects
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