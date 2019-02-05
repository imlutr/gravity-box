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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.pixelsToMeters
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.ColorScheme.currentDarkColor
import ro.luca1152.gravitybox.utils.ColorScheme.darkerDarkColor
import ro.luca1152.gravitybox.utils.MyButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class LevelEditorScreen(private val batch: Batch = Injekt.get(),
                        private val manager: AssetManager = Injekt.get()) : KtxScreen {
    private lateinit var skin: Skin
    private lateinit var gameStage: Stage
    private lateinit var uiStage: Stage
    private lateinit var root: Table

    override fun show() {
        // Initialize lateinit vars
        skin = manager.get<Skin>("skins/uiskin.json")
        gameStage = Stage(ExtendViewport(720f.pixelsToMeters, 1280f.pixelsToMeters))
        uiStage = Stage(ExtendViewport(720f, 1280f), batch)

        // The bottom half should not be transparent, so the grid lines can't be seen through it
        uiStage.addActor(addBackgroundColorToBottomHalf(ColorScheme.currentLightColor))

        // Add UI widgets
        root = createRootTable().apply { uiStage.addActor(this) }
        root.add(createTopHalf()).grow().row()
        root.add(createBottomHalf()).grow().padTop(185f)

        // Handle input
        Gdx.input.inputProcessor = uiStage
    }

    private fun createRootTable() = Table().apply { setFillParent(true) }

    private fun addBackgroundColorToBottomHalf(color: Color) = Image(manager.get<Texture>("graphics/pixel.png")).apply {
        this.color = color
        width = 720f
        height = 1280f / 2f - 185f / 2f
    }

    private fun createTopHalf(): Table {
        fun createBackButton() = MyButton(skin, "small-button", "back-button",
                currentDarkColor, darkerDarkColor, Runnable { uiStage.addAction(sequence(fadeOut(.5f), run(Runnable { Injekt.get<MyGame>().setScreen<MainMenuScreen>() }))) }).apply {
            imageCell!!.padLeft(-5f)
        }

        return Table().apply {
            val topRow = Table().apply {
                add(createBackButton()).pad(50f).expand().left()
            }
            add(topRow).expand().fillX().top()
        }
    }

    private fun createBottomHalf(): Table {
        fun createMidLine() = Image(manager.get<Texture>("graphics/pixel.png")).apply { color = ColorScheme.currentDarkColor }

        fun createUndoButton() = MyButton(skin, "small-button", "undo-icon", currentDarkColor, darkerDarkColor)

        fun createEraseButton() = MyButton(skin, "small-button", "erase-icon", currentDarkColor, darkerDarkColor)

        fun createMoveButton() = MyButton(skin, "small-button", "move-icon", currentDarkColor, darkerDarkColor)

        fun createRedoButton() = MyButton(skin, "small-button", "redo-icon", currentDarkColor, darkerDarkColor)

        return Table().apply {
            add(createMidLine()).padLeft(-1f).width(720f).height(15f).expand().top().row()

            val bottomRow = Table().apply {
                defaults().space(35f)
                add(createUndoButton())
                add(createEraseButton())
                add(createMoveButton())
                add(createRedoButton())
            }
            add(bottomRow).fillX().bottom().pad(50f)
        }
    }

    private fun update(delta: Float) {
        gameStage.act()
        uiStage.act(delta)
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        gameStage.draw()
        uiStage.draw()
    }
}