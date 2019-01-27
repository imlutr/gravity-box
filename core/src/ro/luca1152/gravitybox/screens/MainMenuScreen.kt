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
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MainMenuScreen(batch: Batch = Injekt.get(),
                     private val manager: AssetManager = Injekt.get()) : KtxScreen {
    private val uiStage = Stage(ExtendViewport(720f, 1280f), batch)

    override fun show() {
        val skin = manager.get<Skin>("skins/uiskin.json")

        val table = Table(skin)
        table.width = uiStage.width
        table.setPosition(0f, uiStage.height - 288f)
        table.center().top()
        uiStage.addActor(table)

        val titleImage = Image(skin, "gravity-box").apply {
            color = ColorScheme.currentDarkColor
        }
        val playButton = ImageButton(skin, "play-button").apply {
            image.color = ColorScheme.darkerDarkColor
            color = ColorScheme.darkerDarkColor
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    LevelSelectorScreen.chosenlevel = 1
                    Injekt.get<MyGame>().setScreen<PlayScreen>()
                }
            })
        }
        val titleGroup = Group().apply {
            addActor(titleImage)
            addActor(playButton)
            setSize(titleImage.width, titleImage.height)
            titleImage.setPosition(0f, 18f)
            playButton.setPosition(width / 2f - playButton.width / 2f, 0f)
        }
        table.add(titleGroup).height(titleGroup.height + 18f).padBottom(84f).row()

        val levelsButton = TextButton("LEVELS", skin, "menu-button").apply {
            color = ColorScheme.darkerDarkColor
            label.color = ColorScheme.darkerDarkColor
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    Injekt.get<MyGame>().setScreen<LevelSelectorScreen>()
                }
            })
        }
        table.add(levelsButton).width(464f).height(112f).padBottom(84f).row()

        val settingsButton = TextButton("OPTIONS", skin, "menu-button").apply {
            color = ColorScheme.darkerDarkColor
            label.color = ColorScheme.darkerDarkColor
        }
        table.add(settingsButton).width(464f).height(112f)

        Gdx.input.inputProcessor = uiStage
    }

    private fun update(delta: Float) {
        uiStage.act(delta)
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        uiStage.draw()
    }
}