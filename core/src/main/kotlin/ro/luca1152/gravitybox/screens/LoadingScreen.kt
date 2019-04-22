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
import ktx.log.info
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.assets.loaders.Text
import ro.luca1152.gravitybox.utils.assets.loaders.TextLoader
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.UIViewport
import ro.luca1152.gravitybox.utils.kotlin.clearScreen
import ro.luca1152.gravitybox.utils.kotlin.setScreen
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class LoadingScreen(
    private val manager: AssetManager = Injekt.get(),
    private val game: MyGame = Injekt.get(),
    private val uiStage: UIStage = Injekt.get(),
    private val uiViewport: UIViewport = Injekt.get()
) : KtxScreen {
    private var loadingAssetsTimer = 0f
    private var loadedEditorMaps = false
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
        manager.setLoader(Text::class.java, TextLoader(InternalFileHandleResolver()))
        for (i in 1..MyGame.LEVELS_NUMBER) {
            manager.load<Text>("maps/game/map-$i.json")
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
            // The editor maps are loaded after because they use a different loader, as they are stored locally,
            // not internally, and the AssetManager doesn't support two loaders for a class
            if (!loadedEditorMaps) {
                loadEditorMaps()
                loadedEditorMaps = true
                return
            } else {
                logLoadingTime()
                addScreens()
                showPlayScreen()
            }
        }
    }

    private fun loadEditorMaps() {
        manager.setLoader(Text::class.java, TextLoader(LocalFileHandleResolver()))
        Gdx.files.local("maps/editor").list().forEach {
            manager.load<Text>(it.path())
        }
    }

    private fun logLoadingTime() = info { "Finished loading assets in ${(loadingAssetsTimer * 100).toInt() / 100f}s." }

    // They are added here and not in [MyGame] because adding a screen automatically initializes it, initialization which
    // may use assets, such as [Skin]s or [Texture]s, that are loaded here.
    private fun addScreens() {
        game.run {
            addScreen(LevelEditorScreen())
            addScreen(PlayScreen())
        }
    }

    private fun showPlayScreen() {
        game.setScreen(
            TransitionScreen(PlayScreen::class.java, fadeOutCurrentScreen = false, clearScreenWithBlack = true)
        )
    }
}