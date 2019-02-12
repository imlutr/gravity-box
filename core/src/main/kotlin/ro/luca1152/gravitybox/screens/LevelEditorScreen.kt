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
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.MapComponent
import ro.luca1152.gravitybox.entities.EntityFactory
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.listeners.WorldContactListener
import ro.luca1152.gravitybox.systems.editor.*
import ro.luca1152.gravitybox.systems.game.ColorSyncSystem
import ro.luca1152.gravitybox.systems.game.ImageRenderingSystem
import ro.luca1152.gravitybox.systems.game.UpdateGameCameraSystem
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.ButtonType
import ro.luca1152.gravitybox.utils.ui.ClickButton
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import ro.luca1152.gravitybox.utils.ui.ToggleButton
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
    private lateinit var toggledButton: Reference<ToggleButton>

    // Game
    private val world = World(Vector2(0f, MapComponent.GRAVITY), true)
    private val gameEventSignal = Signal<GameEvent>()

    // Input
    private lateinit var inputMultiplexer: InputMultiplexer

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
        toggledButton = Reference()
        inputMultiplexer = InputMultiplexer()

        // Add UI widgets
        root = createRootTable().apply {
            uiStage.addActor(this)
            add(createLeftColumn()).growY().expandX().left()
            add(createRightColumn()).growY().expandX().right()
        }

        // Make everything fade in
        uiStage.addAction(sequence(fadeOut(0f), fadeIn(1f)))
        gameStage.addAction(sequence(fadeOut(0f), fadeIn(1f)))

        // Handle input
        inputMultiplexer.addProcessor(uiStage)
    }

    private fun createRootTable() = Table().apply {
        setFillParent(true)
        padLeft(62f).padRight(62f)
        padBottom(110f).padTop(110f)
    }


    private fun createLeftColumn(): Table {
        fun createUndoButton(toggledButton: Reference<ToggleButton>) = ClickButton(skin, "small-button").apply {
            addIcon("undo-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
        }

        fun createEraseButton(toggledButton: Reference<ToggleButton>) = ToggleButton(skin, "small-button").apply {
            addIcon("erase-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
        }

        fun createMoveToolButton(toggledButton: Reference<ToggleButton>) = ToggleButton(skin, "small-button").apply {
            addIcon("move-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
            type = ButtonType.MOVE_TOOL_BUTTON
            isToggled = true
        }

        fun createPlaceToolButton(toggledButton: Reference<ToggleButton>) = ToggleButton(skin, "small-button").apply {
            addIcon("platform-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
            type = ButtonType.PLACE_TOOL_BUTTON
        }

        fun createBackButton(toggledButton: Reference<ToggleButton>) = ClickButton(skin, "small-button").apply {
            addIcon("back-icon")
            iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
            setToggleOffEveryOtherButton(true)
            addClickRunnable(Runnable {
                uiStage.addAction(
                        sequence(
                                fadeOut(.5f),
                                run(Runnable { Injekt.get<MyGame>().setScreen<LevelSelectorScreen>() })
                        )
                )
            })
        }

        return Table().apply {
            // If I don't pass [toggledButton] as an argument it doesn't work
            add(createUndoButton(toggledButton)).top().space(50f).row()
            add(createEraseButton(toggledButton)).top().space(50f).row()
            add(createMoveToolButton(toggledButton)).top().space(50f).row()
            add(createPlaceToolButton(toggledButton)).top().row()
            add(createBackButton(toggledButton)).expand().bottom()
        }
    }

    private fun createRightColumn(): Table {
        fun createRedoButton() = ClickButton(skin, "small-button").apply {
            addIcon("redo-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        }

        return Table().apply {
            add(createRedoButton()).expand().top()
        }
    }

    private fun createGame() {
        // Dependency injection
        Injekt.run {
            addSingleton(world)
            addSingleton(gameEventSignal)
            addSingleton(inputMultiplexer)
            addSingleton(skin)
            addSingleton(OverlayCamera)
            addSingleton(OverlayViewport)
            addSingleton(OverlayStage)
        }

        // Provide own implementation for what happens after Box2D collisions
        world.setContactListener(WorldContactListener())

        // Create entities
        val buttonListener = EntityFactory.createButtonListenerEntity(toggledButton)

        // Handle Input
        inputMultiplexer.addProcessor(gameStage)
        inputMultiplexer.addProcessor(Injekt.get<OverlayStage>())

        // Add systems
        engine.run {
            addSystem(ColorSyncSystem())
            addSystem(ObjectSelectionSystem(buttonListener))
            addSystem(ObjectPlacementSystem(buttonListener))
            addSystem(ZoomingSystem(buttonListener))
            addSystem(PanningSystem(buttonListener))
            addSystem(UpdateGameCameraSystem())
            addSystem(OverlayCameraSyncSystem())
            addSystem(OverlayPositioningSystem())
            addSystem(GridRenderingSystem())
            addSystem(ImageRenderingSystem())
            addSystem(OverlayRenderingSystem())
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