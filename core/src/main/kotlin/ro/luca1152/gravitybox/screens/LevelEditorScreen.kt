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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
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
import ro.luca1152.gravitybox.components.game.level
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
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import ro.luca1152.gravitybox.utils.ui.button.ButtonType
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import ro.luca1152.gravitybox.utils.ui.button.ClickTextButton
import ro.luca1152.gravitybox.utils.ui.button.ToggleButton
import ro.luca1152.gravitybox.utils.ui.popup.PopUp
import ro.luca1152.gravitybox.utils.ui.popup.TextPopUp
import ro.luca1152.gravitybox.utils.ui.popup.YesNoTextPopUp
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
    private val saveConfirmationPopUp = YesNoTextPopUp(
        520f, 400f,
        "Are you sure you want to save the level?",
        skin, "semi-bold-50",
        ColorScheme.currentDarkColor, yesIsHighlighted = true
    ).apply {
        yesClickRunnable = Runnable {
            uiStage.addActor(levelSavedTextPopUp)
            levelEntity.map.saveMap()
            remove()
        }
    }
    private val levelSavedTextPopUp = TextPopUp(
        450f, 250f,
        "Level saved successfully.",
        skin, "semi-bold-50", ColorScheme.currentDarkColor
    )
    private val deleteConfirmationPopUp = YesNoTextPopUp(
        520f, 400f,
        "Are you sure you want to delete the level #[x]?",
        skin, "semi-bold-50",
        ColorScheme.currentDarkColor, yesIsHighlighted = true
    )
    private val loadConfirmationPopUp = YesNoTextPopUp(
        520f, 400f,
        "Are you sure you want to load the level #[x]?",
        skin, "semi-bold-50",
        ColorScheme.currentDarkColor, yesIsHighlighted = true
    )
    private var loadLevelPopUp = PopUp(0f, 0f, skin)
    private val settingsPopUp = PopUp(500f, 400f, skin).apply {
        val saveButton = ClickTextButton("Save", skin, "text-only-button").apply {
            upColor = ColorScheme.currentDarkColor
            downColor = ColorScheme.darkerDarkColor
            clickRunnable = Runnable {
                uiStage.addActor(saveConfirmationPopUp)
            }
        }
        val loadButton = ClickTextButton("Load", skin, "text-only-button").apply {
            upColor = ColorScheme.currentDarkColor
            downColor = ColorScheme.darkerDarkColor
            clickRunnable = Runnable {
                loadLevelPopUp = createLoadLevelPopUp()
                uiStage.addActor(loadLevelPopUp)
            }
        }
        val resizeButton = ClickTextButton(
            "Resize",
            skin,
            "text-only-button"
        ).apply {
            upColor = ColorScheme.currentDarkColor
            downColor = ColorScheme.darkerDarkColor
        }

        widget.run {
            add(saveButton).growX().expandY().top().row()
            add(loadButton).growX().expandY().top().row()
            add(resizeButton).expandY().growX().top()
        }
    }
    private val settingsButton = ClickButton(skin, "small-button").apply {
        addIcon("settings-icon")
        setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
        addClickRunnable(Runnable {
            uiStage.addActor(settingsPopUp)
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
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity

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
        finishEntity = FinishEntity.createEntity(
            1,
            platformEntity.image.rightX - FinishEntity.WIDTH / 2f,
            platformEntity.image.topY + FinishEntity.HEIGHT / 2f,
            blinkEndlessly = false
        )
        playerEntity = PlayerEntity.createEntity(
            0,
            platformEntity.image.leftX + PlayerEntity.WIDTH / 2f,
            platformEntity.image.topY + PlayerEntity.HEIGHT / 2f
        )
        centerCameraOnPlatform(platformEntity)
    }

    private fun getFirstUnusedLevelId(): Int {
        val usedIds = Array<Int>()
        Gdx.files.local("maps/editor").list().forEach {
            val jsonData = if (manager.isLoaded(it.path())) {
                manager.get<Text>(it.path()).string
            } else {
                Gdx.files.local(it.path()).readString()
            }
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

    private fun centerCameraOnPlayer() {
        val playerPosition = playerEntity.body.body.worldCenter
        gameCamera.position.set(playerPosition.x, playerPosition.y, 0f)
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
            val mapFactory = Json().fromJson(MapFactory::class.java, jsonData)
            val tableRow = Table(skin).apply {
                val rowLeft = createLoadLevelRowLeft(mapFactory, getLastEditedString(it.nameWithoutExtension()))
                val rowRight = createLoadLevelRowRight(it.path(), mapFactory.id)
                add(rowLeft).growX().expandY().left()
                add(rowRight).expand().right()
            }
            add(tableRow).width(width).growX().spaceTop(25f).row()
        }
    }

    private fun createLoadLevelRowLeft(mapFactory: MapFactory, lastEditedString: String) = Table(skin).apply {
        val levelIdLabel = Label("Level #${mapFactory.id}", skin, "bold-57", Color.WHITE).apply {
            color = ColorScheme.currentDarkColor
        }
        val lastEditedLabel = Label(lastEditedString, skin, "bold-37", Color.WHITE).apply {
            color = ColorScheme.currentDarkColor
        }
        add(levelIdLabel).grow().left().row()
        add(lastEditedLabel).grow().left().row()
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                levelIdLabel.color = ColorScheme.darkerDarkColor
                lastEditedLabel.color = ColorScheme.darkerDarkColor
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                levelIdLabel.color = ColorScheme.currentDarkColor
                lastEditedLabel.color = ColorScheme.currentDarkColor
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                loadConfirmationPopUp.run {
                    textLabel.setText("Are you sure you want to load the level #${mapFactory.id}?")
                    uiStage.addActor(this)
                    yesClickRunnable = Runnable {
                        levelEntity.map.loadMap(mapFactory, playerEntity, finishEntity)
                        centerCameraOnPlayer()
                        removeSettingsPopUp = true
                        loadLevelPopUp.remove()
                    }
                }
            }
        })
    }

    private fun createLoadLevelRowRight(levelFilePath: String, levelId: Int) = Table(skin).apply {
        add(ClickButton(skin, "simple-button").apply {
            addIcon("trash-can-icon")
            setColors(ColorScheme.currentDarkColor, ColorScheme.darkerDarkColor)
            addClickRunnable(Runnable {
                deleteConfirmationPopUp.run {
                    textLabel.setText("Are you sure you want to delete the level #$levelId?")
                    uiStage.addActor(this)
                    yesClickRunnable = Runnable {
                        Gdx.files.local(levelFilePath).delete()
                        loadLevelPopUp.remove()
                        updateLoadLevelPopUp = true
                        val newId = getFirstUnusedLevelId()
                        levelEntity.run {
                            map.levelId = newId
                            level.levelId = newId
                        }
                    }
                }
            })
        })
    }

    private var updateLoadLevelPopUp = false
    private var removeSettingsPopUp = false

    private fun createLoadLevelPopUp() = PopUp(520f, 500f, skin).apply {
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
        updatePopUps()
    }

    /** Updates pop-ups in the game loop to avoid recursion problems. */
    private fun updatePopUps() {
        if (updateLoadLevelPopUp) {
            updateLoadLevelPopUp = false
            loadLevelPopUp = createLoadLevelPopUp()
            uiStage.addActor(loadLevelPopUp)
        }
        if (removeSettingsPopUp) {
            removeSettingsPopUp = false
            settingsPopUp.remove()
        }
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