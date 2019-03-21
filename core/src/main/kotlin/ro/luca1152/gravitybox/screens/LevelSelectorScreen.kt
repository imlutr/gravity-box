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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.app.KtxScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.clearScreen
import ro.luca1152.gravitybox.utils.kotlin.setScreen
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.HorizontalSlidingPane
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.roundToInt

class LevelSelectorScreen(
    manager: AssetManager = Injekt.get(),
    private val uiStage: UIStage = Injekt.get(),
    private val game: MyGame = Injekt.get()
) : KtxScreen {
    companion object {
        var chosenLevel = 0
    }

    private val skin = manager.get(Assets.uiSkin)
    private val bigEmptyStar = Image(skin, "big-empty-star").apply {
        color = Colors.gameColor
    }
    private val starsCountLabel = DistanceFieldLabel("0/45", skin, "bold", 65f, Colors.gameColor)
    private val leftArrow = Image(skin.getDrawable("left-arrow-icon")).apply {
        color = Colors.gameColor
        isVisible = false
    }
    private val horizontalSlidingPane = HorizontalSlidingPane(uiStage.camera.viewportWidth, 1000f).apply {
        var level = 0
        for (pageNumber in 1..4) {
            val page = Table().apply {
                defaults().space(50f)
                addPage(this)
            }
            for (i in 1..12) {
                level++
                page.add(createLevelButton(level, this))
                if (level % 3 == 0) {
                    page.row()
                }
            }
        }
    }
    private val rightArrow = Image(skin.getDrawable("right-arrow-icon")).apply {
        color = Colors.gameColor
    }
    private val backButton = ClickButton(skin, "small-button").apply {
        addIcon("back-icon")
        iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            game.setScreen(TransitionScreen(MainMenuScreen::class.java))
        })
    }
    private val levelEditorButton = ClickButton(skin, "small-button").apply {
        addIcon("pencil-icon")
        iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            game.setScreen(TransitionScreen(LevelEditorScreen::class.java))
        })
    }
    private val topRow = Table().apply {
        add(bigEmptyStar).padRight(20f).expand().right()
        add(starsCountLabel).right()
    }
    private val middleRow = Table().apply {
        add(leftArrow).left()
        add(horizontalSlidingPane).expand()
        add(rightArrow).right()
    }
    private val bottomRow = Table().apply {
        add(backButton).expand().left()
        add(levelEditorButton).right()
    }
    private val rootTable = Table().apply {
        setFillParent(true)
        padLeft(62f).padRight(62f)
        padBottom(110f).padTop(110f)
        add(topRow).growX().row()
        add(middleRow).grow().row()
        add(bottomRow).bottom().growX()
    }

    private fun createLevelButtonStars(): Table {
        val stars = Table()
        for (i in 0 until 3) {
            val star = Image(skin, "empty-star").apply { color = Colors.gameColor }
            stars.add(star).spaceRight(3f)
        }
        return stars
    }

    private fun createLevelButton(
        level: Int,
        horizontalSlidingPane: HorizontalSlidingPane
    ) = Button(skin, "small-button").apply button@{
        color = Colors.gameColor
        top().padTop(18f)
        val numberLabel =
            DistanceFieldLabel(level.toString(), skin, "extra-bold", 57f, Colors.gameColor).apply {
                this@button.add(this).expand().center().row()
            }
        val stars = createLevelButtonStars().apply {
            this@button.add(this).bottom().padBottom(23f)
        }

        fun setAllColors(color: Color) {
            numberLabel.color = color
            this.color.set(color)
            stars.cells.forEach { it.actor.color.set(color) }
        }

        addAction(forever(run(Runnable {
            if (horizontalSlidingPane.isPanning)
                setAllColors(Colors.gameColor)
        })))

        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                addAction(
                    sequence(
                        delay(.05f),
                        run(Runnable {
                            if (!horizontalSlidingPane.isPanning)
                                setAllColors(Colors.uiDownColor)
                        })
                    )
                )
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                setAllColors(Colors.gameColor)
                if (!horizontalSlidingPane.isPanning && isOver(this@button, x, y)) {
                    chosenLevel = Math.min(level, MyGame.LEVELS_NUMBER)
                    game.setScreen(TransitionScreen(PlayScreen::class.java))
                }
            }
        })
    }

    override fun show() {
        uiStage.addActor(rootTable)
        overrideBackKey()
    }

    private fun overrideBackKey() {
        Gdx.input.run {
            isCatchBackKey = true
            inputProcessor = InputMultiplexer(uiStage, object : InputAdapter() {
                override fun keyDown(keycode: Int): Boolean {
                    if (keycode == Input.Keys.BACK) {
                        run(Runnable {
                            game.setScreen(TransitionScreen(MainMenuScreen::class.java))
                        })
                    }
                    return false
                }
            })
        }
    }

    override fun render(delta: Float) {
        update()
        clearScreen(Colors.bgColor)
        uiStage.draw()
    }

    private fun update() {
        uiStage.act()
        updatePageArrowsVisibility()
    }

    private fun updatePageArrowsVisibility() {
        leftArrow.isVisible = when {
            horizontalSlidingPane.currentPage.roundToInt() == 1 -> false
            else -> true
        }
        rightArrow.isVisible = when {
            horizontalSlidingPane.currentPage.roundToInt() == horizontalSlidingPane.pageCount -> false
            else -> true
        }
    }
}
