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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.app.KtxScreen
import ktx.assets.load
import ktx.inject.Context
import ktx.log.info
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.assets.loaders.MapPack
import ro.luca1152.gravitybox.utils.assets.loaders.MapPackLoader
import ro.luca1152.gravitybox.utils.assets.loaders.Text
import ro.luca1152.gravitybox.utils.assets.loaders.TextLoader
import ro.luca1152.gravitybox.utils.kotlin.*

class LoadingScreen(private val context: Context) : KtxScreen {
    // Injected objects
    private val manager: AssetManager = context.inject()
    private val game: MyGame = context.inject()
    private val uiStage: UIStage = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val overlayViewport: OverlayViewport = context.inject()

    private var loadingAssetsTimer = 0f
    private val finishedLoadingAssets
        get() = manager.update()
    private val gravityBoxText = Image(Texture(Gdx.files.internal("graphics/gravity-box-text.png")).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }).apply {
        x = uiViewport.worldWidth / 2f - prefWidth / 2f
        y = uiViewport.worldHeight / 2f - prefHeight / 2f
    }

    override fun show() {
        showSplashScreen()
        loadGraphics()
        loadGameMaps()
        loadEditorMaps()
    }

    private fun showSplashScreen() {
        uiStage.addActor(gravityBoxText)
    }

    private fun loadGraphics() {
        manager.run {
            load(Assets.uiSkin)
            load(Assets.tileset)
        }
    }

    private fun loadGameMaps() {
        manager.setLoader(MapPack::class.java, MapPackLoader(InternalFileHandleResolver()))
        manager.load(Assets.gameMaps)
    }

    private fun loadEditorMaps() {
        manager.setLoader(Text::class.java, TextLoader(LocalFileHandleResolver()))
        Gdx.files.local("maps/editor").list().forEach {
            manager.load<Text>(it.path())
        }
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(Color.BLACK)
        uiStage.draw()
    }

    private fun update(delta: Float) {
        loadingAssetsTimer += delta
        if (finishedLoadingAssets) {
            logLoadingTime()
            addScreens()
            showPlayScreen()
        }
    }

    private fun logLoadingTime() = info { "Finished loading assets in ${(loadingAssetsTimer * 100).toInt() / 100f}s." }

    // They are added here and not in [MyGame] because adding a screen automatically initializes it, initialization which
    // may use assets, such as [Skin]s or [Texture]s, that are loaded here.
    private fun addScreens() {
        game.run {
            addScreen(LevelEditorScreen(context))
            addScreen(PlayScreen(context))
        }
    }

    private fun showPlayScreen() {
        game.setScreen(
            TransitionScreen(context, PlayScreen::class.java, fadeOutCurrentScreen = false, clearScreenWithBlack = true)
        )
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, false)
        overlayViewport.update(width, height, false)
    }
}