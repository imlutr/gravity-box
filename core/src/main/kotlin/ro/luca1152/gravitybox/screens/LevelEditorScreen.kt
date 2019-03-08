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
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.collections.contains
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
import ro.luca1152.gravitybox.utils.assets.Text
import ro.luca1152.gravitybox.utils.json.MapFactory
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.*
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.get
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LevelEditorScreen(
    private val engine: PooledEngine = Injekt.get(),
    private val manager: AssetManager = Injekt.get(),
    private val gameStage: GameStage = Injekt.get(),
    private val gameViewport: GameViewport = Injekt.get(),
    private val gameCamera: GameCamera = Injekt.get(),
    private val uiStage: UIStage = Injekt.get(),
    private val inputMultiplexer: InputMultiplexer = Injekt.get()
) : KtxScreen {
    private val skin = manager.get<Skin>("skins/uiskin.json")
    private val toggledButton = Reference<ToggleButton>()
    private val undoButton = ClickButton(skin, "small-button").apply {
        addIcon("undo-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setOpaque(true)
        addClickRunnable(Runnable {
            undoRedoEntity.undoRedo.undo()
        })
    }
    private val redoButton = ClickButton(skin, "small-button").apply {
        addIcon("redo-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setOpaque(true)
        addClickRunnable(Runnable {
            undoRedoEntity.undoRedo.redo()
        })
    }
    private val placeToolButton = ToggleButton(skin, "small-button").apply {
        addIcon("platform-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setToggledButtonReference(this@LevelEditorScreen.toggledButton)
        type = ButtonType.PLACE_TOOL_BUTTON
        setOpaque(true)
    }
    val moveToolButton = ToggleButton(skin, "small-button").apply {
        addIcon("move-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setToggledButtonReference(this@LevelEditorScreen.toggledButton)
        type = ButtonType.MOVE_TOOL_BUTTON
        isToggled = true
        setOpaque(true)
    }
    private val backButton = ClickButton(skin, "small-button").apply {
        addIcon("back-icon")
        iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setToggledButtonReference(this@LevelEditorScreen.toggledButton)
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
    private val playButton = ClickButton(skin, "small-button").apply {
        addIcon("play-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        setToggledButtonReference(this@LevelEditorScreen.toggledButton)
        setToggleOffEveryOtherButton(true)
        addClickRunnable(Runnable {
            engine.addSystem(PlayingSystem(this@LevelEditorScreen))
        })
        setOpaque(true)
    }
    private val levelSavedPopUp = PopUp(450f, 250f, skin).apply {
        widget.run {
            val label = Label(
                "Level saved\nsuccessfully.",
                this@LevelEditorScreen.skin,
                "semi-bold-50",
                ColorScheme.currentDarkColor
            )
            label.setAlignment(Align.center, Align.center)
            add(label)
        }
    }

    private fun getLastEditedString(fileNameWithoutExtension: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        val levelDate = formatter.parse(fileNameWithoutExtension)
        val currentDate = Date(TimeUtils.millis())

        val diffInMills = Math.abs(currentDate.time - levelDate.time)
        val diffInYears = TimeUnit.DAYS.convert(diffInMills, TimeUnit.MILLISECONDS) / 365
        val diffInMonths = TimeUnit.DAYS.convert(diffInMills, TimeUnit.MILLISECONDS) / 30
        val diffInWeeks = TimeUnit.DAYS.convert(diffInMills, TimeUnit.MILLISECONDS) / 7
        val diffInDays = TimeUnit.DAYS.convert(diffInMills, TimeUnit.MILLISECONDS)
        val diffInHours = TimeUnit.HOURS.convert(diffInMills, TimeUnit.MILLISECONDS)
        val diffInMinutes = TimeUnit.MINUTES.convert(diffInMills, TimeUnit.MILLISECONDS)
        val diffInSeconds = TimeUnit.SECONDS.convert(diffInMills, TimeUnit.MILLISECONDS)

        return when {
            diffInYears != 0L -> "$diffInYears year${if (diffInYears > 1) "s" else ""} ago"
            diffInMonths != 0L -> "$diffInMonths month${if (diffInMonths > 1) "s" else ""} ago"
            diffInWeeks != 0L -> "$diffInWeeks week${if (diffInWeeks > 1) "s" else ""} ago"
            diffInDays != 0L -> "$diffInDays day${if (diffInDays > 1) "s" else ""} ago"
            diffInHours != 0L -> "$diffInHours hour${if (diffInHours > 1) "s" else ""} ago"
            diffInMinutes != 0L -> "$diffInMinutes minute${if (diffInMinutes > 1) "s" else ""} ago"
            else -> "$diffInSeconds second${if (diffInSeconds > 1) "s" else ""} ago"
        }
    }
    private val settingsButton = ClickButton(skin, "small-button").apply {
        addIcon("settings-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            val popUp = PopUp(500f, 400f, skin)

            val saveButton = ClickTextButton("Save", skin, "text-only-button").apply {
                upColor = ColorScheme.currentDarkColor
                downColor = ColorScheme.darkerDarkColor
                clickRunnable = Runnable {
                    levelEntity.map.saveMap()
                    uiStage.addActor(levelSavedPopUp)
                }
            }
            val loadButton = ClickTextButton("Load", skin, "text-only-button").apply {
                upColor = ColorScheme.currentDarkColor
                downColor = ColorScheme.darkerDarkColor
                clickRunnable = Runnable {
                    uiStage.addActor(createLoadLevelPopUp())
                }
            }
            val resizeButton = ClickTextButton("Resize", skin, "text-only-button").apply {
                upColor = ColorScheme.currentDarkColor
                downColor = ColorScheme.darkerDarkColor
            }

            popUp.widget.run {
                add(saveButton).expand().top().row()
                add(loadButton).expand().top().row()
                add(resizeButton).expand().top()
            }

            uiStage.addActor(popUp)
        })
        setOpaque(true)
    }
    private val leftColumn = Table().apply {
        add(undoButton).top().space(50f).row()
        add(moveToolButton).top().space(50f).row()
        add(placeToolButton).top().row()
        add(backButton).expand().bottom()
    }
    private val middleColumn = Table().apply {
        add(playButton).expand().bottom()
    }
    private val rightColumn = Table().apply {
        add(redoButton).expand().top().row()
        add(settingsButton).expand().bottom()
    }
    val rootTable = Table().apply {
        setFillParent(true)
        padLeft(62f).padRight(62f)
        padBottom(110f).padTop(110f)
        add(leftColumn).growY().expandX().left()
        add(middleColumn).growY().expandX().center()
        add(rightColumn).growY().expandX().right()
    }
    private var screenIsHidden = false
    private val gameEventSignal = Signal<GameEvent>()
    private lateinit var inputEntity: Entity
    private lateinit var undoRedoEntity: Entity
    private lateinit var levelEntity: Entity

    override fun show() {
        uiStage.addActor(rootTable)
        addDependencies()
        resetVariables()
        createGame()
        createUI()
        handleAllInput()
    }

    private fun resetVariables() {
        moveToolButton.run {
            isToggled = true
            toggledButton.set(this)
        }
        inputMultiplexer.clear()
        screenIsHidden = false
    }

    private fun createGame() {
        createGameEntities()
        handleGameInput()
        addGameSystems()
    }

    private fun addDependencies() {
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
        levelEntity = LevelEntity.createEntity(getFirstUnusedLevelId(), 16, 19)
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

    private fun getFirstUnusedLevelId(): Int {
        val usedIds = Array<Int>()
        Gdx.files.local("maps/editor").list().forEach {
            val jsonData = manager.get<Text>(it.path()).string
            val mapFactory = Json().fromJson(MapFactory::class.java, jsonData)
            usedIds.add(mapFactory.id)
        }
        var id = 0
        while (usedIds.contains(id))
            id++
        return id
    }

    private fun centerCameraOnPlatform(platformEntity: Entity) {
        val platformPosition = platformEntity.body.body.worldCenter
        val deltaY = 2f
        gameCamera.position.set(platformPosition.x, platformPosition.y + deltaY, 0f)
    }

    private fun handleGameInput() {
        inputMultiplexer.run {
            addProcessor(Injekt.get<OverlayStage>())
            addProcessor(gameStage)
        }
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

    private fun createLoadLevelTable(width: Float) = Table(skin).apply {
        val levels = Gdx.files.local("maps/editor").list().apply {
            sortByDescending { it.path() }
        }
        levels.forEach {
            val jsonData = if (manager.isLoaded(it.path())) {
                manager.get<Text>(it.path()).string
            } else {
                Gdx.files.local(it.path()).readString()
            }
            val levelFactory = Json().fromJson(MapFactory::class.java, jsonData)
            val tableRow = Table(skin).apply {
                val rowLeft = Table(skin).apply {
                    val lastEdited = getLastEditedString(it.nameWithoutExtension())
                    add(Label("Level #${levelFactory.id}", skin, "bold-57", ColorScheme.currentDarkColor)).left().row()
                    add(Label(lastEdited, skin, "bold-37", ColorScheme.currentDarkColor)).left().row()
                }
                val rowRight = Table(skin).apply {
                    add(ClickButton(skin, "simple-button").apply {
                        addIcon("trash-can-icon")
                        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
                    })
                }
                add(rowLeft).expand().left()
                add(rowRight).expand().right()
            }
            add(tableRow).width(width).growX().spaceTop(25f).row()
        }
    }

    private fun createLoadLevelPopUp() = PopUp(500f, 400f, skin).apply {
        val scrollPane = ScrollPane(createLoadLevelTable(430f)).apply {
            setupOverscroll(50f, 80f, 200f)
        }
        widget.add(scrollPane).expand().top()
    }

    private fun createUI() {
        fadeEverythingIn()
        handleUIInput()
    }

    private fun fadeEverythingIn() {
        uiStage.addAction(sequence(fadeOut(0f), fadeIn(1f)))
        gameStage.addAction(sequence(fadeOut(0f), fadeIn(1f)))
    }

    private fun handleUIInput() {
        // [index] is 0 so UI input is handled first, otherwise the buttons can't be pressed
        inputMultiplexer.addProcessor(0, uiStage)
    }

    private fun handleAllInput() {
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun update(delta: Float) {
        uiStage.act(delta)
        if (screenIsHidden)
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
        update(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun hide() {
        screenIsHidden = true
        engine.run {
            removeAllSystems()
            removeAllEntities()
        }
        uiStage.clear()
    }

    override fun dispose() {
        uiStage.dispose()
        gameStage.dispose()
    }
}