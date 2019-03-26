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
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.app.KtxScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.clearScreen
import ro.luca1152.gravitybox.utils.kotlin.setScreen
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import ro.luca1152.gravitybox.utils.ui.popup.TextPopUp
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MainMenuScreen(
    manager: AssetManager = Injekt.get(),
    game: MyGame = Injekt.get(),
    private val uiStage: UIStage = Injekt.get()
) : KtxScreen {
    private val skin = manager.get(Assets.uiSkin)
    private val logoImage = Image(skin, "gravity-box").apply {
        color = Colors.gameColor
    }
    private val playButton = ClickButton(skin, "big-button").apply {
        addIcon("play-button-empty").run {
            icon!!.addAction(
                forever(
                    sequence(
                        delay(.5f),
                        fadeOut(1f),
                        fadeIn(1f)
                    )
                )
            )
        }
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            game.setScreen(TransitionScreen(LevelSelectorScreen::class.java))
        })
    }
    private val logo = Table().apply {
        add(logoImage).row()
        add(playButton).padTop(-214f + 18f).padLeft(-4f)
    }
    private val rateButton = ClickButton(skin, "small-button").apply {
        addIcon("heart-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            uiStage.addActor(
                TextPopUp(
                    450f, 250f,
                    "[TO DO]",
                    skin, "bold", 50f, Colors.gameColor
                )
            )
        })
    }
    private val settingsButton = ClickButton(skin, "small-button").apply {
        addIcon("settings-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            uiStage.addActor(
                TextPopUp(
                    450f, 250f,
                    "[TO DO]",
                    skin, "bold", 50f, Colors.gameColor
                )
            )
        })
    }
    private val bottomRow = Table().apply {
        add(rateButton).expandX().left()
        add(settingsButton).expandX().right()
    }
    private val rootTable = Table().apply {
        padLeft(50f).padRight(50f)
        padBottom(110f).padTop(110f)
        setFillParent(true)
        add(logo).expand().padTop(140f).row()
        add(bottomRow).bottom().growX().pad(0f).padLeft(12f).padRight(12f)
    }

    override fun show() {
        uiStage.addActor(rootTable)
        handleInput()
    }

    private fun handleInput() {
        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        updateColors()
        uiStage.act()
        clearScreen(Colors.bgColor)
        uiStage.draw()
    }

    private fun updateColors() {
        logoImage.color = Colors.gameColor
    }
}