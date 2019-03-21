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
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import ktx.app.KtxGame
import ro.luca1152.gravitybox.components.game.MapComponent
import ro.luca1152.gravitybox.screens.LoadingScreen
import ro.luca1152.gravitybox.utils.kotlin.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

/** The main class of the game. */
class MyGame : KtxGame<Screen>() {
    companion object {
        const val LEVELS_NUMBER = 9
    }

    var transitionOldScreen: Screen? = null

    override fun create() {
        Box2D.init()
        initializeDependencyInjection()
        addScreen(LoadingScreen())
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
            addSingleton(OverlayCamera)
            addSingleton(OverlayViewport)
            addSingleton(OverlayStage)
            addSingleton(UICamera)
            addSingleton(UIStage)
            addSingleton(UIViewport)
            addSingleton(World(Vector2(0f, MapComponent.GRAVITY), true))
        }
    }

    override fun dispose() {
        super.dispose() // Disposes every screen
        disposeHeavyInjectedObjects()
    }

    private fun disposeHeavyInjectedObjects() {
        Injekt.run {
            get<Batch>().dispose()
            get<AssetManager>().dispose()
            get<ShapeRenderer>().dispose()
            get<World>().dispose()
        }
    }
}