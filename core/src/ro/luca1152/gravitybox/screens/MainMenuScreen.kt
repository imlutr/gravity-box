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
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MainMenuScreen(private val manager: AssetManager = Injekt.get()) : KtxScreen {
    private val uiStage = Stage(FitViewport(720f, 1280f))
    private lateinit var playButton: Button

    override fun show() {
        val skin = manager.get<Skin>("skins/uiskin.json")

        val table = Table(skin)
        table.width = uiStage.width
        table.setPosition(0f, uiStage.height / 2f)
        table.center()
        uiStage.addActor(table)

        playButton = Button(skin, "play-button")
        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Injekt.get<MyGame>().setScreen<PlayScreen>()
            }
        })
        table.add(playButton)

        Gdx.input.inputProcessor = uiStage
    }

    private fun update(delta: Float) {
        uiStage.act(delta)
        playButton.color = ColorScheme.currentDarkColor
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        uiStage.draw()
    }
}