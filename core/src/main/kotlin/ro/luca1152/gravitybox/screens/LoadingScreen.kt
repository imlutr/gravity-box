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
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.getAsset
import ktx.assets.load
import ktx.log.info
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.assets.Text
import ro.luca1152.gravitybox.utils.assets.TextLoader
import ro.luca1152.gravitybox.utils.kotlin.setScreen
import ro.luca1152.gravitybox.utils.ui.ColorScheme.currentLightColor
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class LoadingScreen(
    private val manager: AssetManager = Injekt.get(),
    private val game: MyGame = Injekt.get()
) : KtxScreen {
    private var loadingAssetsTimer = 0f
    private val finishedLoadingAssets
        get() = manager.update()

    override fun show() {
        loadGraphics()
        loadAudio()
        loadMaps()
    }

    private fun loadGraphics() {
        manager.run {
            load<Skin>("skins/uiskin.json")
            load<Texture>("graphics/player.png")
            load<Texture>("graphics/bullet.png")
            load<Texture>("graphics/bullet.png")
            load<Texture>("graphics/circle.png")
            load<Texture>("graphics/finish.png")
            load<Texture>("graphics/pixel.png")
        }
    }

    private fun loadAudio() {
        manager.run {
            load<Music>("audio/music.mp3")
            load<Sound>("audio/level-finished.wav")
            load<Sound>("audio/bullet-wall-collision.wav")
        }
    }

    private fun loadMaps() {
        loadGameMaps()
        loadEditorMaps()
    }

    private fun loadGameMaps() {
        Gdx.files.local("maps/game").list().forEach {
            manager.run {
                setLoader(Text::class.java, TextLoader(LocalFileHandleResolver()))
                load<Text>(it.path())
            }
        }
    }

    private fun loadEditorMaps() {
        Gdx.files.local("maps/editor").list().forEach {
            manager.run {
                setLoader(Text::class.java, TextLoader(LocalFileHandleResolver()))
                load<Text>(it.path())
            }
        }
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(currentLightColor.r, currentLightColor.g, currentLightColor.b)
    }

    private fun update(delta: Float) {
        loadingAssetsTimer += delta
        if (finishedLoadingAssets) {
            logLoadingTime()
            smoothTextures()
            addScreens()
            game.setScreen(TransitionScreen(MainMenuScreen::class.java, false))
        }
    }

    private fun smoothTextures() {
        manager.run {
            getAsset<Texture>("graphics/player.png").setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
            getAsset<Texture>("graphics/bullet.png").setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
            getAsset<Texture>("graphics/circle.png").setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
            getAsset<Texture>("graphics/finish.png").setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear
            )
        }
    }

    /**
     * Adds screens to the [KtxGame] so [KtxGame.setScreen] works.
     *
     * They are added here and not in [MyGame] because adding a screen automatically initializes it, where assets
     * could be referenced, such as [Skin]s or [Texture]s.
     *
     * If the screens where added in [MyGame], then every variable which referenced assets loading here should have been
     * declared as lateinit var, and initialized in [KtxScreen.show].
     */
    private fun addScreens() {
        game.run {
            addScreen(MainMenuScreen())
            addScreen(LevelEditorScreen())
            addScreen(LevelSelectorScreen())
            addScreen(PlayScreen())
        }
    }

    private fun logLoadingTime() {
        info { "Finished loading assets in ${(loadingAssetsTimer * 100).toInt() / 100f}s." }
    }
}