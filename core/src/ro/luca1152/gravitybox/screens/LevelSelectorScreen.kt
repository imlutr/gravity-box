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
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.HorizontalSlidingPane
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class LevelSelectorScreen(batch: Batch = Injekt.get(),
                          private val manager: AssetManager = Injekt.get()) : KtxScreen {
    companion object {
        var chosenlevel = 0
    }

    private val uiStage = Stage(ExtendViewport(720f, 1280f), batch)
    private lateinit var skin: Skin
    private lateinit var root: Table
    private lateinit var topRow: Table
    private lateinit var horizontalSlidingPane: HorizontalSlidingPane
    private lateinit var bottomRow: Table
    private val pageIndicatorCircles = ArrayList<Image>()

    override fun show() {
        uiStage.actors.removeAll(uiStage.actors, true)
        pageIndicatorCircles.clear()

        skin = manager.get<Skin>("skins/uiskin.json")
        createRootTable()
        createTopRow()
        createHorizontalSlidingPane()
        createBottomRow()
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

    private fun createRootTable() {
        root = Table().apply {
            pad(50f)
            setFillParent(true)
        }
        uiStage.addActor(root)
    }

    private fun createTopRow() {
        fun createBackButton() = Button(skin, "back-button").apply {
            color = ColorScheme.currentDarkColor
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    color = ColorScheme.darkerDarkColor
                    return true
                }

                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    color = ColorScheme.currentDarkColor
                    if (isOver(this@apply, x, y)) {
                        uiStage.addAction(sequence(
                                fadeOut(.5f),
                                run(Runnable { Injekt.get<MyGame>().setScreen<MainMenuScreen>() })
                        ))
                    }
                }
            })
        }

        fun createBigEmptyStar() = Image(skin, "big-empty-star").apply {
            color = ColorScheme.currentDarkColor
        }

        fun createStarsNumberLabel() = Label("0/45", skin, "bold-65", ColorScheme.currentDarkColor)

        topRow = Table()
        root.add(topRow).growX().row()

        // Add widgets to the topRow
        topRow.add(createBackButton()).expandX().left()
        topRow.add(createBigEmptyStar()).right()
        topRow.add(createStarsNumberLabel()).right().padLeft(20f)
    }

    private fun createHorizontalSlidingPane() {
        fun createPage(): Table {
            val page = Table().apply {
                defaults().space(50f)

                // If it's not touchable, the HorizontalSlidingPane slides only if you tap on the buttons, and not on the whole area.
                touchable = Touchable.enabled
            }
            horizontalSlidingPane.addPage(page)
            return page
        }

        fun createStars(): Table {
            val stars = Table()
            for (i in 0 until 3) {
                val star = Image(skin, "empty-star").apply { color = ColorScheme.currentDarkColor }
                stars.add(star).spaceRight(3f)
            }
            return stars
        }

        fun createNumberLabel(level: Int) = Label(level.toString(), skin, "bold-57", ColorScheme.currentDarkColor)

        fun createLevelButton(level: Int) = Button(skin, "small-button").apply {
            color = ColorScheme.currentDarkColor
            top().padTop(18f)

            val numberLabel = createNumberLabel(level)
            add(numberLabel).expand().center().row()

            val stars = createStars()
            add(stars).bottom().padBottom(23f)

            fun setAllColors(color: Color) {
                numberLabel.style.fontColor = color
                this.color.set(color)
                stars.cells.forEach { it.actor.color.set(color) }
            }

            addAction(forever(run(Runnable {
                if (horizontalSlidingPane.isPanning)
                    setAllColors(ColorScheme.currentDarkColor)
            })))

            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    addAction(sequence(
                            delay(.05f),
                            run(Runnable {
                                if (!horizontalSlidingPane.isPanning)
                                    setAllColors(ColorScheme.darkerDarkColor)
                            })
                    ))
                    return true
                }

                override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                    setAllColors(ColorScheme.currentDarkColor)
                    if (!horizontalSlidingPane.isPanning && isOver(this@apply, x, y)) {
                        chosenlevel = Math.min(level, MyGame.LEVELS_NUMBER)
                        uiStage.addAction(sequence(
                                fadeOut(.5f),
                                run(Runnable { Injekt.get<MyGame>().setScreen<PlayScreen>() })
                        ))
                    }
                }
            })
        }

        horizontalSlidingPane = HorizontalSlidingPane(uiStage.camera.viewportWidth, 1000f)
        root.add(horizontalSlidingPane).expand().row()

        var level = 1

        for (pageNumber in 1..4) {
            val page = createPage()
            for (i in 1..15) {
                val levelButton = createLevelButton(level)
                page.add(levelButton)

                if (level % 3 == 0)
                    page.row()
                level++
            }
        }
    }

    private fun createBottomRow() {
        fun createSmallEmptyCircle() = Image(skin, "small-empty-circle").apply { color = ColorScheme.currentDarkColor }
        fun createSmallFullCircle() = Image(skin, "small-full-circle").apply { color = ColorScheme.currentDarkColor }

        bottomRow = Table().apply { defaults().space(10f) }
        root.add(bottomRow).bottom().expandX().padBottom(20f)

        createSmallFullCircle().run {
            bottomRow.add(this)
            pageIndicatorCircles.add(this)
        }
        for (i in 1 until horizontalSlidingPane.pagesCount) {
            createSmallEmptyCircle().run {
                bottomRow.add(this)
                pageIndicatorCircles.add(this)
            }
        }
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        uiStage.draw()
    }

    private fun update(delta: Float) {
        uiStage.act(delta)
        updatePageIndicatorCircles()
    }

    private fun updatePageIndicatorCircles() {
        for (i in 0 until horizontalSlidingPane.pagesCount)
            pageIndicatorCircles[i].drawable = skin.getDrawable("small-empty-circle")
        pageIndicatorCircles[Math.round(horizontalSlidingPane.currentPage) - 1].drawable = skin.getDrawable("small-full-circle")
    }
}
