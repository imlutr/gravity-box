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
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxGame
import ro.luca1152.gravitybox.components.game.MapComponent
import ro.luca1152.gravitybox.screens.*
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

/** The main class of the game. */
class MyGame : KtxGame<Screen>() {
    companion object {
        const val LEVELS_NUMBER = 1
    }

    override fun create() {
        Box2D.init()
        initializeDependencyInjection()
        addScreens()
        setScreen<LoadingScreen>()
    }

    private fun initializeDependencyInjection() {
        Injekt.run {
            addSingleton(this@MyGame)
            addSingleton(SpriteBatch() as Batch)
            addSingleton(AssetManager())
            addSingleton(GameCamera)
            addSingleton(GameStage)
            addSingleton(GameViewport)
            addSingleton(InputMultiplexer())
            addSingleton(PooledEngine())
            addSingleton(ShapeRenderer())
            addSingleton(UICamera)
            addSingleton(UIStage)
            addSingleton(UIViewport)
            addSingleton(World(Vector2(0f, MapComponent.GRAVITY), true))
        }
    }

    /** Adds screens to the [KtxGame] so [setScreen] works.*/
    private fun addScreens() {
        addScreen(LoadingScreen())
        addScreen(MainMenuScreen())
        addScreen(LevelEditorScreen())
        addScreen(LevelSelectorScreen())
        addScreen(PlayScreen())
    }

    private fun initializeColorScheme() {
        ColorScheme.hue = MathUtils.random(0, 360).toFloat()
        ColorScheme.updateColors()
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

val Int.metersToPixels: Float
    get() = this * PPM

val Float.pixelsToMeters: Float
    get() = this / PPM

val Float.metersToPixels: Float
    get() = this * PPM