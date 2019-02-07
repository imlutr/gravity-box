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

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.MapComponent
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.listeners.WorldContactListener
import ro.luca1152.gravitybox.systems.GridRenderingSystem
import ro.luca1152.gravitybox.systems.ImageRenderingSystem
import ro.luca1152.gravitybox.systems.PanningSystem
import ro.luca1152.gravitybox.utils.ColorScheme
import ro.luca1152.gravitybox.utils.GameStage
import ro.luca1152.gravitybox.utils.GameViewport
import ro.luca1152.gravitybox.utils.MyButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

class LevelEditorScreen(private val engine: PooledEngine = Injekt.get(),
                        private val batch: Batch = Injekt.get(),
                        private val manager: AssetManager = Injekt.get(),
                        private val gameStage: GameStage = Injekt.get(),
                        private val gameViewport: GameViewport = Injekt.get()) : KtxScreen {
    // UI
    private lateinit var skin: Skin
    private lateinit var uiStage: Stage
    private lateinit var root: Table

    // Game
    private val world = World(Vector2(0f, MapComponent.GRAVITY), true)
    private val gameEventSignal = Signal<GameEvent>()

    // Input
    private val inputMultiplexer: InputMultiplexer = InputMultiplexer()

    override fun show() {
        createUI()
        createGame()

        // Handle input
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun createUI() {
        // Initialize lateinit vars
        skin = manager.get<Skin>("skins/uiskin.json")
        uiStage = Stage(ExtendViewport(720f, 1280f), batch)

        // The bottom half should not be transparent, so the grid lines can't be seen through it
        uiStage.addActor(addBackgroundColorToBottomHalf(ColorScheme.currentLightColor))

        // Add UI widgets
        root = createRootTable().apply {
            uiStage.addActor(this)
            add(createTopHalf()).grow().row()
            add(createBottomHalf()).grow().padTop(185f)
        }

        // Handle input
        inputMultiplexer.addProcessor(uiStage)
    }

    private fun createGame() {
        // Dependency injection
        Injekt.run {
            addSingleton(world)
            addSingleton(gameEventSignal)
            addSingleton(inputMultiplexer)
        }

        // Provide own implementation for what happens after collisions
        world.setContactListener(WorldContactListener(gameEventSignal))

        // Add systems
        engine.run {
            addSystem(GridRenderingSystem())
            addSystem(PanningSystem())
            addSystem(ImageRenderingSystem())
        }
    }

    private fun createRootTable() = Table().apply { setFillParent(true) }

    private fun addBackgroundColorToBottomHalf(color: Color) = Image(manager.get<Texture>("graphics/pixel.png")).apply {
        this.color = color
        width = 720f
        height = 1280f / 2f - 185f / 2f
    }

    private fun createTopHalf(): Table {
        fun createBackButton() = MyButton(skin, "small-button").apply {
            addIcon("back-icon")
            iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setOpaque(true)
            addClickRunnable(Runnable {
                uiStage.addAction(sequence(
                        fadeOut(.5f),
                        run(Runnable { Injekt.get<MyGame>().setScreen<MainMenuScreen>() })
                ))
            })
        }

        return Table().apply {
            val topRow = Table().apply {
                add(createBackButton()).pad(50f).expand().left()
            }
            add(topRow).expand().fillX().top()
        }
    }

    private fun createBottomHalf(): Table {
        fun createMidLine() = Image(manager.get<Texture>("graphics/pixel.png")).apply {
            color = ColorScheme.currentDarkColor
        }

        fun createUndoButton() = MyButton(skin, "small-button").apply {
            addIcon("undo-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }

        fun createEraseButton() = MyButton(skin, "small-button").apply {
            addIcon("erase-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }

        fun createMoveButton() = MyButton(skin, "small-button").apply {
            addIcon("move-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }

        fun createRedoButton() = MyButton(skin, "small-button").apply {
            addIcon("redo-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }

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
        engine.update(delta)
        uiStage.act(delta)
        gameStage.camera.update()
    }

    override fun render(delta: Float) {
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        update(delta) // This MUST be after clearScreen() because draw functions may be called in engine.update()
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun dispose() {
        if (::uiStage.isInitialized)
            uiStage.dispose()
        gameStage.dispose()
    }
}