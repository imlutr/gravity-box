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
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class LevelSelectorScreen(batch: Batch = Injekt.get(),
                          private val manager: AssetManager = Injekt.get()) : KtxScreen {
    companion object {
        var chosenlevel = 0
    }

    private val uiStage = Stage(ExtendViewport(720f, 1280f), batch)

    override fun show() {
        uiStage.actors.removeAll(uiStage.actors, true)

        val skin = manager.get<Skin>("skins/uiskin.json")

        val table = Table().apply {
            pad(50f)
            setFillParent(true)
        }
        uiStage.addActor(table)

        val topRow = Table()
        table.add(topRow).growX().row()

        val backButton = Button(skin, "back-button").apply {
            color = ColorScheme.darkerDarkColor
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    uiStage.addAction(sequence(
                            fadeOut(.5f),
                            run(Runnable { Injekt.get<MyGame>().setScreen<MainMenuScreen>() })
                    ))
                }
            })
        }
        topRow.add(backButton).expandX().left()

        val bigEmptyStar = Image(skin, "big-empty-star").apply {
            color = ColorScheme.darkerDarkColor
        }
        topRow.add(bigEmptyStar).right()

        val starsNumber = Label("0/45", skin, "bold-65", ColorScheme.darkerDarkColor)
        topRow.add(starsNumber).right().padLeft(20f)

        val buttons = Table().apply {
            width = 600f
            center().top()
            defaults().space(50f)
        }
        table.add(buttons).expand().row()

        for (level in 1..15) {
            val button = Button(skin, "small-button").apply {
                color = ColorScheme.darkerDarkColor
                top().padTop(18f)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        chosenlevel = level
                        uiStage.addAction(sequence(
                                fadeOut(.5f),
                                run(Runnable { Injekt.get<MyGame>().setScreen<PlayScreen>() })
                        ))
                    }
                })
            }

            val numberLabel = Label(level.toString(), skin, "bold-57", ColorScheme.darkerDarkColor)
            button.add(numberLabel).expand().center().row()

            val stars = Table()
            for (i in 0 until 3) {
                val star = Image(skin, "empty-star").apply { color = ColorScheme.darkerDarkColor }
                stars.add(star).spaceRight(3f)
            }
            button.add(stars).bottom().padBottom(23f)

            buttons.add(button)
            if (level % 3 == 0)
                buttons.row()
        }

        val bottomRow = Table().apply {
            defaults().space(10f)
        }
//        table.debug = true
        table.add(bottomRow).bottom().expandX().padBottom(20f)

        for (i in 0 until 4) {
            val smallCircle = when (i) {
                0 -> Image(skin, "small-full-circle")
                else -> Image(skin, "small-empty-circle")
            }.apply { color = ColorScheme.darkerDarkColor }

            bottomRow.add(smallCircle)
        }


        uiStage.addAction(sequence(fadeOut(0f), fadeIn(1f)))

        Gdx.input.inputProcessor = InputMultiplexer(uiStage, object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK) {
                    uiStage.addAction(sequence(
                            fadeOut(.5f),
                            run(Runnable { Injekt.get<MyGame>().setScreen<MainMenuScreen>() })
                    ))
                }
                return false
            }
        })
        Gdx.input.isCatchBackKey = true
    }

    override fun render(delta: Float) {
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        uiStage.act(delta)
        uiStage.draw()
    }
}