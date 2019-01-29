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
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
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
        uiStage.actors.removeAll(uiStage.actors, true)

        val skin = manager.get<Skin>("skins/uiskin.json")

        val table = Table(skin).apply {
            setFillParent(true)
            center()
        }

        val titleImage = Image(skin, "gravity-box").apply {
            color = ColorScheme.currentDarkColor
        }
        val playButton = Button(skin, "big-button").apply {
            color = ColorScheme.darkerDarkColor
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    LevelSelectorScreen.chosenlevel = 1
                    uiStage.addAction(sequence(
                            fadeOut(.5f),
                            run(Runnable { Injekt.get<MyGame>().setScreen<LevelSelectorScreen>() })
                    ))
                }
            })
        }
        val playButtonImageEmpty = Image(skin, "play-button").apply {
            color = ColorScheme.darkerDarkColor
            addAction(repeat(-1, sequence(
                    fadeOut(1f),
                    fadeIn(1f)
            )))
        }
        val titleGroup = Group().apply {
            addActor(titleImage)
            addActor(playButtonImageEmpty)
            addActor(playButton)

            setSize(titleImage.width, titleImage.height)

            titleImage.setPosition(0f, 18f)
            playButton.setPosition(width / 2f - playButton.width / 2f, 0f)
            playButtonImageEmpty.setPosition(width / 2f - playButtonImageEmpty.width / 2f, 33f)
        }

        uiStage.addActor(table)
        table.add(titleGroup).height(titleGroup.height + 18f)

        uiStage.addAction(sequence(
                fadeOut(0f),
                fadeIn(.5f))
        )

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