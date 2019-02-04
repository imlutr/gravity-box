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

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
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

class LevelEditorScreen(private val batch: Batch = Injekt.get(),
                        private val manager: AssetManager = Injekt.get()) : KtxScreen {
    private lateinit var uiStage: Stage
    private lateinit var skin: Skin
    private lateinit var root: Table

    override fun show() {
        uiStage = Stage(ExtendViewport(720f, 1280f), batch)
        skin = manager.get<Skin>("skins/uiskin.json")

        root = createRootTable().apply { uiStage.addActor(this) }
        root.add(createBackButton()).pad(50f).expand().top().left().row()
        root.add(createBottomHalf()).grow().padTop(185f)
    }

    private fun createRootTable() = Table().apply { setFillParent(true) }

    private fun createBackButton() = Button(skin, "small-button").apply {
        color = ColorScheme.currentDarkColor
        val backIcon = Image(skin, "back-button").apply {
            color = ColorScheme.currentDarkColor
        }
        add(backIcon).padLeft(-5f)

        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                color = ColorScheme.darkerDarkColor
                backIcon.color = ColorScheme.darkerDarkColor
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                color = ColorScheme.currentDarkColor
                backIcon.color = ColorScheme.currentDarkColor
                if (isOver(this@apply, x, y)) {
                    uiStage.addAction(sequence(
                            fadeOut(.5f),
                            run(Runnable { Injekt.get<MyGame>().setScreen<MainMenuScreen>() })
                    ))
                }
            }
        })
    }

    private fun createBottomHalf(): Table {
        fun createMidLine() = Image(manager.get<Texture>("graphics/pixel.png")).apply {
            width = uiStage.viewport.worldWidth
            height = 32f
            color = ColorScheme.currentDarkColor
        }

        fun createUndoButton() = Button(skin, "small-button").apply {
            color = ColorScheme.currentDarkColor
            val undoIcon = Image(skin, "undo-icon").apply {
                color = ColorScheme.currentDarkColor
            }
            add(undoIcon)
        }

        fun createEraseButton() = Button(skin, "small-button").apply {
            color = ColorScheme.currentDarkColor
            val eraseIcon = Image(skin, "erase-icon").apply {
                color = ColorScheme.currentDarkColor
            }
            add(eraseIcon)
        }

        fun createMoveButton() = Button(skin, "small-button").apply {
            color = ColorScheme.currentDarkColor
            val moveIcon = Image(skin, "move-icon").apply {
                color = ColorScheme.currentDarkColor
            }
            add(moveIcon)
        }

        fun createRedoButton() = Button(skin, "small-button").apply {
            color = ColorScheme.currentDarkColor
            val redoIcon = Image(skin, "redo-icon").apply {
                color = ColorScheme.currentDarkColor
            }
            add(redoIcon)
        }

        val bottomHalf = Table()
        bottomHalf.add(createMidLine()).width(720f).height(32f).expand().top().row()

        val bottomRow = Table().apply {
            defaults().space(35f)
            add(createUndoButton())
            add(createEraseButton())
            add(createMoveButton())
            add(createRedoButton())
        }
        bottomHalf.add(bottomRow).fill().bottom().pad(50f)

        return bottomHalf
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