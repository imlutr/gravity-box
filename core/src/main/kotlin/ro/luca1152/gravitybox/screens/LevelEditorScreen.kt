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

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.editor.undoRedo
import ro.luca1152.gravitybox.components.game.body
import ro.luca1152.gravitybox.components.game.image
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.entities.editor.InputEntity
import ro.luca1152.gravitybox.entities.editor.UndoRedoEntity
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.LevelEntity
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.events.GameEvent
import ro.luca1152.gravitybox.systems.editor.*
import ro.luca1152.gravitybox.systems.game.ColorSyncSystem
import ro.luca1152.gravitybox.systems.game.DebugRenderingSystem
import ro.luca1152.gravitybox.systems.game.ImageRenderingSystem
import ro.luca1152.gravitybox.systems.game.UpdateGameCameraSystem
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get

class LevelEditorScreen(
    private val engine: PooledEngine = Injekt.get(),
    private val manager: AssetManager = Injekt.get(),
    private val gameStage: GameStage = Injekt.get(),
    private val gameViewport: GameViewport = Injekt.get(),
    private val gameCamera: GameCamera = Injekt.get(),
    private val uiStage: UIStage = Injekt.get(),
    private val inputMultiplexer: InputMultiplexer = Injekt.get()
) : KtxScreen {
    // UI
    private var screenIsHiding = false
    private lateinit var skin: Skin
    val root: Table = createRootTable()
    private val toggledButton = Reference<ToggleButton>()
    private lateinit var undoButton: ClickButton
    private lateinit var redoButton: ClickButton
    lateinit var moveToolButton: ToggleButton

    // Game
    private val gameEventSignal = Signal<GameEvent>()
    private lateinit var inputEntity: Entity
    private lateinit var undoRedoEntity: Entity

    override fun show() {
        resetVariables()
        createGame()
        createUI()
        handleAllInput()
    }

    private fun resetVariables() {
        uiStage.clear()
        skin = manager.get<Skin>("skins/uiskin.json")
        root.clear()
        toggledButton.set(null)
        inputMultiplexer.clear()
        screenIsHiding = false
    }

    private fun createGame() {
        addSingletonsToDependencyInjection()
        createGameEntities()
        handleGameInput()
        addGameSystems()
    }

    private fun addSingletonsToDependencyInjection() {
        Injekt.run {
            addSingleton(gameEventSignal)
            addSingleton(skin)
            addSingleton(OverlayCamera)
            addSingleton(OverlayViewport)
            addSingleton(OverlayStage)
        }
    }

    private fun createGameEntities() {
        inputEntity = InputEntity.createEntity(toggledButton)
        undoRedoEntity = UndoRedoEntity.createEntity()
        val levelEntity = LevelEntity.createEntity(0, 16, 19)
        val platformEntity = PlatformEntity.createEntity(
            2,
            levelEntity.map.widthInTiles / 2f,
            levelEntity.map.widthInTiles / 2f - .5f,
            4f
        )
        FinishEntity.createEntity(
            1,
            platformEntity.image.rightX - FinishEntity.WIDTH / 2f,
            platformEntity.image.topY + FinishEntity.HEIGHT / 2f,
            blinkEndlessly = false
        )
        PlayerEntity.createEntity(
            0,
            platformEntity.image.leftX + PlayerEntity.WIDTH / 2f,
            platformEntity.image.topY + PlayerEntity.HEIGHT / 2f
        )
        centerCameraOnPlatform(platformEntity)
    }

    private fun centerCameraOnPlatform(platformEntity: Entity) {
        val platformPosition = platformEntity.body.body.worldCenter
        val deltaY = 2f
        gameCamera.position.set(platformPosition.x, platformPosition.y + deltaY, 0f)
    }

    private fun handleGameInput() {
        inputMultiplexer.addProcessor(Injekt.get<OverlayStage>())
        inputMultiplexer.addProcessor(gameStage)
    }

    fun addGameSystems() {
        engine.run {
            addSystem(LevelSavingSystem())
            addSystem(UndoRedoSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(ColorSyncSystem())
            addSystem(ObjectPlacementSystem())
            addSystem(ZoomingSystem())
            addSystem(PanningSystem())
            addSystem(ObjectSelectionSystem())
            addSystem(UpdateGameCameraSystem())
            addSystem(OverlayCameraSyncSystem())
            addSystem(OverlayPositioningSystem())
            addSystem(TouchableBoundsSyncSystem())
            addSystem(GridRenderingSystem())
            addSystem(ImageRenderingSystem())
            addSystem(OverlayRenderingSystem())
            addSystem(DebugRenderingSystem())
        }
    }

    private fun createUI() {
        root.run {
            uiStage.addActor(this)
            add(createLeftColumn()).growY().expandX().left()
            add(createMiddleColumn()).growY().expandX().center()
            add(createRightColumn()).growY().expandX().right()
        }

        // Make everything fade in
        uiStage.addAction(sequence(fadeOut(0f), fadeIn(1f)))
        gameStage.addAction(sequence(fadeOut(0f), fadeIn(1f)))

        handleUIInput()
    }

    private fun handleUIInput() {
        // [index] is 0 so UI input is handled first, otherwise the buttons can't be pressed
        inputMultiplexer.addProcessor(0, uiStage)
    }

    fun createRootTable() = Table().apply {
        setFillParent(true)
        padLeft(62f).padRight(62f)
        padBottom(110f).padTop(110f)
    }

    private fun createUndoButton() = ClickButton(skin, "small-button").apply {
        addIcon("undo-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setOpaque(true)
        addClickRunnable(Runnable {
            undoRedoEntity.undoRedo.undo()
        })
    }

    private fun createRedoButton() = ClickButton(skin, "small-button").apply {
        addIcon("redo-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setOpaque(true)
        addClickRunnable(Runnable {
            undoRedoEntity.undoRedo.redo()
        })
    }

    private fun createMoveToolButton(toggledButton: Reference<ToggleButton>) =
        ToggleButton(skin, "small-button").apply {
            addIcon("move-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
            type = ButtonType.MOVE_TOOL_BUTTON
            isToggled = true
            setOpaque(true)
        }

    private fun createLeftColumn(): Table {
        undoButton = createUndoButton()
        moveToolButton = createMoveToolButton(toggledButton)

        fun createPlaceToolButton(toggledButton: Reference<ToggleButton>) = ToggleButton(skin, "small-button").apply {
            addIcon("platform-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
            type = ButtonType.PLACE_TOOL_BUTTON
            setOpaque(true)
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
            setOpaque(true)
        }

        return Table().apply {
            // If I don't pass [toggledButton] as an argument it doesn't work
            add(undoButton).top().space(50f).row()
            add(moveToolButton).top().space(50f).row()
            add(createPlaceToolButton(toggledButton)).top().row()
            add(createBackButton(toggledButton)).expand().bottom()
        }
    }

    private fun createMiddleColumn(): Table {
        fun createPlayButton(toggledButton: Reference<ToggleButton>) = ClickButton(skin, "small-button").apply {
            addIcon("play-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            setToggledButtonReference(toggledButton)
            setToggleOffEveryOtherButton(true)
            addClickRunnable(Runnable {
                engine.addSystem(PlayingSystem(this@LevelEditorScreen))
            })
            setOpaque(true)
        }

        return Table().apply {
            add(createPlayButton(toggledButton)).expand().bottom()
        }
    }


    private fun createRightColumn(): Table {
//        fun createSettingsPopUp() = Table().apply {
//            val frameImage = Image(this@LevelEditorScreen.skin.getDrawable("pop-up-frame")).apply {
//                setSize(500f, 400f)
//                color = ColorScheme.currentDarkColor
//            }
//            val insideImage = Image(manager.get<Texture>("graphics/pixel.png")).apply {
//                val borderWidth = 14f
//                setSize(frameImage.width - 2 * borderWidth, frameImage.height - 2 * borderWidth)
//                setPosition(borderWidth, borderWidth)
//                color = ColorScheme.currentLightColor
//            }
//            addActor(frameImage)
//            addActor(insideImage)
//            setSize(frameImage.width, frameImage.height)
//            touchable = Touchable.enabled
//            addListener(object : ClickListener() {
//                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
//                    return true
//                }
//            })
////            add(frameImage).size(frameImage.width, frameImage.height)
//            setPosition(uiStage.width / 2f - width / 2f, uiStage.height / 2f - height / 2f)
//        }

        fun createSettingsButton() = ClickButton(skin, "small-button").apply {
            addIcon("settings-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            addClickRunnable(Runnable {
                val popUp = PopUp(500f, 400f)
                uiStage.addActor(popUp)
            })
            setOpaque(true)
        }

        redoButton = createRedoButton()
        return Table().apply {
            add(redoButton).expand().top().row()
            add(createSettingsButton()).expand().bottom()
        }
    }

    private fun handleAllInput() {
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun update(delta: Float) {
        uiStage.act(delta)
        if (screenIsHiding)
            return

        engine.update(delta)
        gameStage.camera.update()
        updateUndoRedoButtonsColor()
    }

    private fun updateUndoRedoButtonsColor() {
        when (undoRedoEntity.undoRedo.canUndo()) {
            false -> grayOutButton(undoButton)
            true -> resetButtonColor(undoButton)
        }
        when (undoRedoEntity.undoRedo.canRedo()) {
            false -> grayOutButton(redoButton)
            true -> resetButtonColor(redoButton)
        }
    }

    private fun grayOutButton(button: ClickButton) {
        button.run {
            setColors(ColorScheme.currentDarkColor, ColorScheme.currentDarkColor)
            opaqueImage?.color?.a = 0f
            color.a = .3f
        }
    }

    private fun resetButtonColor(button: ClickButton) {
        button.run {
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            opaqueImage?.color?.a = 1f
            color.a = 1f
        }
    }

    override fun render(delta: Float) {
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        update(delta) // This MUST be after clearScreen() because draw functions may be called in engine.update()
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun hide() {
        screenIsHiding = true
        engine.run {
            removeAllSystems()
            removeAllEntities()
        }
    }

    override fun dispose() {
        uiStage.dispose()
        gameStage.dispose()
    }
}