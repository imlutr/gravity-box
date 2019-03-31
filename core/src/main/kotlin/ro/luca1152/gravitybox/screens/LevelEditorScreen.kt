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
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import ktx.app.KtxScreen
import ktx.collections.contains
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.editor.input
import ro.luca1152.gravitybox.components.editor.undoRedo
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.editor.InputEntity
import ro.luca1152.gravitybox.entities.editor.UndoRedoEntity
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.LevelEntity
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.systems.editor.*
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.assets.json.MapFactory
import ro.luca1152.gravitybox.utils.assets.loaders.Text
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.button.*
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
    private val inputMultiplexer: InputMultiplexer = Injekt.get(),
    private val game: MyGame = Injekt.get()
) : KtxScreen {
    private val skin = manager.get(Assets.uiSkin)
    private val toggledButton = Reference<ToggleButton>()
    private val undoButton = ClickButton(skin, "small-button").apply {
        addIcon("undo-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        syncColorsWithColorScheme = false
        addClickRunnable(Runnable {
            undoRedoEntity.undoRedo.undo()
        })
    }
    private val redoButton = ClickButton(skin, "small-button").apply {
        addIcon("redo-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setOpaque(true)
        syncColorsWithColorScheme = false
        addClickRunnable(Runnable {
            undoRedoEntity.undoRedo.redo()
        })
    }
    private val placeToolButton = PaneButton(skin, "small-button").apply paneButton@{
        addIcon("platform-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setToggledButtonReference(this@LevelEditorScreen.toggledButton)
        type = ButtonType.PLACE_TOOL_BUTTON
        setOpaque(true)
        addCellToPane(ClickButton(skin, "small-button").apply {
            addIcon("platform-icon")
            setColors(Colors.gameColor, Colors.uiDownColor)
            setOpaque(true)
            addClickRunnable(createButtonFromPaneRunnable(this@paneButton, this, PlatformComponent::class.java))
        })
        addCellToPane(ClickButton(skin, "small-button").apply {
            addIcon("destroyable-platform-icon")
            setColors(Colors.gameColor, Colors.uiDownColor)
            setOpaque(true)
            addClickRunnable(
                createButtonFromPaneRunnable(this@paneButton, this, DestroyablePlatformComponent::class.java)
            )
        })
    }
    val moveToolButton = ToggleButton(skin, "small-button").apply {
        addIcon("move-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        setToggledButtonReference(this@LevelEditorScreen.toggledButton)
        type = ButtonType.MOVE_TOOL_BUTTON
        isToggled = true
        setOpaque(true)
    }
    private val leaveConfirmationPopUp = YesNoTextPopUp(
        520f, 400f,
        "Are you sure you want to go back to the main menu?",
        skin, "bold", 50f,
        Colors.gameColor, yesIsHighlighted = true
    )
    private val saveBeforeLeavingPopUp = YesNoTextPopUp(
        550f, 350f,
        "Do you want to save the current level?",
        skin, "bold", 50f,
        Colors.gameColor, yesIsHighlighted = true
    ).apply {
        yesClickRunnable = Runnable {
            levelEntity.map.saveMap()
            game.setScreen(TransitionScreen(LevelSelectorScreen::class.java))
        }
        noClickRunnable = Runnable {
            game.setScreen(TransitionScreen(LevelSelectorScreen::class.java))
        }
    }
    private val backButton = ClickButton(skin, "small-button").apply {
        addIcon("back-icon")
        iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
        setColors(Colors.gameColor, Colors.uiDownColor)
        setToggledButtonReference(this@LevelEditorScreen.toggledButton)
        setToggleOffEveryOtherButton(true)
        addClickRunnable(Runnable {
            uiStage.addActor(leaveConfirmationPopUp)
            leaveConfirmationPopUp.yesClickRunnable = if (isEditingNewLevel || isCurrentLevelDeleted) {
                if (undoRedoEntity.undoRedo.canUndo() || isCurrentLevelDeleted) {
                    Runnable {
                        uiStage.addActor(saveBeforeLeavingPopUp)
                    }
                } else {
                    Runnable {
                        game.setScreen(TransitionScreen(LevelSelectorScreen::class.java))
                    }
                }
            } else {
                Runnable {
                    levelEntity.map.saveMap()
                    game.setScreen(TransitionScreen(LevelSelectorScreen::class.java))
                }
            }
        })
        setOpaque(true)
    }
    private val playButton = ClickButton(skin, "small-button").apply {
        addIcon("play-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
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
        skin, "bold", 50f,
        Colors.gameColor,
        yesIsHighlighted = true
    ).apply {
        yesClickRunnable = Runnable {
            isEditingNewLevel = false
            uiStage.addActor(levelSavedTextPopUp)
            levelEntity.map.saveMap(forceSave = true)
            remove()
        }
    }
    private val levelSavedTextPopUp = TextPopUp(
        450f, 250f,
        "Level saved successfully.",
        skin, "bold", 50f, Colors.gameColor
    )
    private val deleteConfirmationPopUp = YesNoTextPopUp(
        520f, 400f,
        "Are you sure you want to delete the level #[x]?",
        skin, "bold", 50f,
        Colors.gameColor, yesIsHighlighted = true
    )
    private val loadConfirmationPopUp = YesNoTextPopUp(
        520f, 400f,
        "Are you sure you want to load the level #[x]?",
        skin, "bold", 50f,
        Colors.gameColor, yesIsHighlighted = true
    )
    private var loadLevelPopUp = PopUp(0f, 0f, skin)
    private val newLevelConfirmationPopUp = YesNoTextPopUp(
        520f, 400f,
        "Are you sure you want to create a new level?",
        skin, "bold", 50f,
        Colors.gameColor, yesIsHighlighted = true
    ).apply {
        yesClickRunnable = Runnable {
            if (isEditingNewLevel) {
                uiStage.addActor(saveLevelBeforeCreationConfirmationPopUp)
            } else {
                isEditingNewLevel = true
                isCurrentLevelDeleted = false
                levelEntity.map.saveMap()
                updateLevelIdToFirstUnused()
                resetMapToInitialState()
            }
        }
    }
    private val saveLevelBeforeCreationConfirmationPopUp = YesNoTextPopUp(
        550f, 350f,
        "Do you want to save the current level?",
        skin, "bold", 50f,
        Colors.gameColor, yesIsHighlighted = true
    ).apply {
        yesClickRunnable = Runnable {
            isEditingNewLevel = true
            isCurrentLevelDeleted = false
            levelEntity.map.saveMap()
            updateLevelIdToFirstUnused()
            resetMapToInitialState()
        }
        noClickRunnable = Runnable {
            isEditingNewLevel = true
            isCurrentLevelDeleted = false
            updateLevelIdToFirstUnused()
            resetMapToInitialState()
        }
    }
    private val cameraPopUpLeftColumn = Table(skin).apply {
        val left = DistanceFieldLabel("Left", skin, "bold", 65f, Colors.gameColor)
        val right = DistanceFieldLabel("Right", skin, "bold", 65f, Colors.gameColor)
        val top = DistanceFieldLabel("Top", skin, "bold", 65f, Colors.gameColor)
        val bottom = DistanceFieldLabel("Bottom", skin, "bold", 65f, Colors.gameColor)
        defaults().spaceBottom(32f)
        add(left).row()
        add(right).row()
        add(top).row()
        add(bottom)
    }
    private val newButton = ClickTextButton("simple-button", skin, "New", "bold", 80f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            uiStage.addActor(newLevelConfirmationPopUp)
        }
    }
    private val saveButton = ClickTextButton("simple-button", skin, "Save", "bold", 80f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            uiStage.addActor(saveConfirmationPopUp)
        }
    }
    private val loadButton = ClickTextButton("simple-button", skin, "Load", "bold", 80f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            loadLevelPopUp = createLoadLevelPopUp()
            uiStage.addActor(loadLevelPopUp)
        }
    }
    private val cameraButton = ClickTextButton("simple-button", skin, "Camera", "bold", 80f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            uiStage.addActor(createCameraPopUp())
        }
    }
    private val playerButton = ClickTextButton("simple-button", skin, "Player", "bold", 80f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            gameCamera.position.set(playerEntity.scene2D.centerX, playerEntity.scene2D.centerY, 0f)
            hideSettingsPopUp = true
        }
    }
    private val settingsPopUp = PopUp(450f, 510f, skin).apply {
        widget.run {
            val buttonsTable = Table(skin).apply {
                add(newButton).growX().expandY().top().row()
                add(saveButton).growX().expandY().top().row()
                add(loadButton).growX().expandY().top().row()
                add(cameraButton).growX().expandY().top().row()
                add(playerButton).growX().expandY().top().row()
            }
            add(buttonsTable).grow()
        }
    }
    private val settingsButton = ClickButton(skin, "small-button").apply {
        addIcon("settings-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
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
    private var isEditingNewLevel = true
    private var updateLoadLevelPopUp = false
    private var hideSettingsPopUp = false
    private var isCurrentLevelDeleted = false
    private lateinit var inputEntity: Entity
    private lateinit var undoRedoEntity: Entity
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity
    private val levelsSavedCount
        get() = Gdx.files.local("maps/editor").list().size

    override fun show() {
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
        isCurrentLevelDeleted = false
        updateLoadLevelPopUp = false
        hideSettingsPopUp = false
        gameCamera.zoom = 0f
    }

    private fun createGame() {
        createGameEntities()
        if (levelsSavedCount == 0) {
            resetMapToInitialState()
        } else {
            loadLastEditedLevel()
        }
        handleGameInput()
        addGameSystems()
    }

    private fun addDependencies() {
        Injekt.run {
            addSingleton(skin)
        }
    }

    private fun createGameEntities() {
        inputEntity = InputEntity.createEntity(toggledButton)
        undoRedoEntity = UndoRedoEntity.createEntity()
        levelEntity = LevelEntity.createEntity(getFirstUnusedLevelId())
        finishEntity = FinishEntity.createEntity(1, blinkEndlessly = false)
        playerEntity = PlayerEntity.createEntity(0)
    }

    private fun loadLastEditedLevel() {
        val lastEditedMapFile = getLastEditedMapFile()
        val mapFactory = getMapFactory(lastEditedMapFile.path())
        levelEntity.map.loadMap(mapFactory, playerEntity, finishEntity)
        isEditingNewLevel = false
        centerCameraOnPlayer()
    }

    private fun resetMapToInitialState() {
        removeAdditionalEntities()
        undoRedoEntity.undoRedo.reset()
        val platformEntity = PlatformEntity.createEntity(2, 0f, .5f, 4f)
        repositionDefaultEntities(platformEntity)
        centerCameraOnPlatform(platformEntity)
        settingsPopUp.remove()
    }

    private fun removeAdditionalEntities() {
        engine.getEntitiesFor(
            Family.all(MapObjectComponent::class.java).exclude(
                PlayerComponent::class.java,
                FinishComponent::class.java
            ).get()
        ).forEach {
            engine.removeEntity(it)
        }
    }

    private fun createButtonFromPaneRunnable(
        placeToolButton: PaneButton,
        button: ClickButton,
        objectType: Class<out Any>
    ) =
        Runnable {
            placeToolButton.clickedOnButtonFromPane()
            placeToolButton.icon!!.drawable = button.icon!!.drawable
            inputEntity.input.placeToolObjectType = objectType
        }

    private fun getLastEditedMapFile(): FileHandle {
        var minLastEditedTime = Long.MAX_VALUE
        var minLastEditedFile = FileHandle("")
        Gdx.files.local("maps/editor").list().forEach {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            val levelDate = formatter.parse(it.nameWithoutExtension())
            val currentDate = Date(TimeUtils.millis())
            val diffInMills = Math.abs(currentDate.time - levelDate.time)
            if (diffInMills < minLastEditedTime) {
                minLastEditedTime = diffInMills
                minLastEditedFile = it
            }
        }
        return minLastEditedFile
    }

    private fun getMapFactory(mapFilePath: String): MapFactory {
        val jsonData = if (manager.isLoaded(mapFilePath)) {
            manager.get<Text>(mapFilePath).string
        } else {
            Gdx.files.local(mapFilePath).readString()
        }
        return Json().fromJson(MapFactory::class.java, jsonData)
    }

    private fun repositionDefaultEntities(platformEntity: Entity) {
        finishEntity.run {
            scene2D.run {
                centerX = platformEntity.scene2D.rightX - FinishEntity.WIDTH / 2f
                centerY = platformEntity.scene2D.topY + FinishEntity.HEIGHT / 2f
            }
            editorObject.isSelected = false
            polygon.update()
        }
        playerEntity.run {
            scene2D.run {
                centerX = platformEntity.scene2D.leftX + PlayerEntity.WIDTH / 2f
                centerY = platformEntity.scene2D.topY + PlayerEntity.HEIGHT / 2f
            }
            editorObject.isSelected = false
            polygon.update()
        }
        levelEntity.run {
            map.run {
                reset()
                updateMapBounds()
            }
            level.forceUpdateMap = true
        }
    }

    private fun getFirstUnusedLevelId(): Int {
        val usedIds = Array<Int>()
        Gdx.files.local("maps/editor").list().forEach {
            val mapFactory = getMapFactory(it.path())
            usedIds.add(mapFactory.id)
        }
        var id = 1
        while (usedIds.contains(id))
            id++
        return id
    }

    private fun updateLevelIdToFirstUnused() {
        val newId = getFirstUnusedLevelId()
        levelEntity.level.levelId = newId
        levelEntity.map.levelId = newId
    }

    private fun centerCameraOnPlatform(platformEntity: Entity) {
        val platformScene2D = platformEntity.scene2D
        val deltaY = 2f
        gameCamera.position.set(platformScene2D.centerX, platformScene2D.centerY + deltaY, 0f)
    }

    private fun centerCameraOnPlayer() {
        val playerScene2D = playerEntity.scene2D
        gameCamera.position.set(playerScene2D.centerX, playerScene2D.centerY, 0f)
    }

    private fun handleGameInput() {
        inputMultiplexer.run {
            addProcessor(Injekt.get<OverlayStage>())
            addProcessor(gameStage)
        }
    }

    fun addGameSystems() {
        engine.run {
            addSystem(UndoRedoSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(ObjectPlacementSystem())
            addSystem(ZoomingSystem())
            addSystem(PanningSystem())
            addSystem(ObjectSelectionSystem())
            addSystem(UpdateGameCameraSystem())
            addSystem(OverlayCameraSyncSystem())
            addSystem(ExtendedTouchSyncSystem())
            addSystem(GridRenderingSystem())
            addSystem(ObjectSnappingSystem())
            addSystem(OverlayPositioningSystem())
            addSystem(RoundedPlatformsSystem())
            addSystem(ColorSyncSystem())
            addSystem(ImageRenderingSystem())
            addSystem(OverlayRenderingSystem())
            addSystem(DebugRenderingSystem())
//            addSystem(PhysicsDebugRenderingSystem())
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
        val levelIdLabel = DistanceFieldLabel(
            "Level #${mapFactory.id}", skin, "bold",
            57f, Colors.gameColor
        )
        val lastEditedLabel = DistanceFieldLabel(
            lastEditedString, skin, "extra-bold",
            37f, Colors.gameColor
        )
        add(levelIdLabel).grow().left().row()
        add(lastEditedLabel).grow().left().row()
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                levelIdLabel.color = Colors.uiDownColor
                lastEditedLabel.color = Colors.uiDownColor
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                levelIdLabel.color = Colors.gameColor
                lastEditedLabel.color = Colors.gameColor
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                loadConfirmationPopUp.run {
                    textLabel.setText("Are you sure you want to load the level #${mapFactory.id}?")
                    uiStage.addActor(this)
                    yesClickRunnable = Runnable {
                        levelEntity.map.loadMap(mapFactory, playerEntity, finishEntity)
                        centerCameraOnPlayer()
                        hideSettingsPopUp = true
                        isEditingNewLevel = false
                        isCurrentLevelDeleted = false
                        loadLevelPopUp.remove()
                    }
                }
            }
        })
    }

    private fun createLoadLevelRowRight(levelFilePath: String, levelId: Int) = Table(skin).apply {
        add(ClickButton(skin, "simple-button").apply {
            addIcon("trash-can-icon")
            setColors(Colors.gameColor, Colors.uiDownColor)
            addClickRunnable(Runnable {
                deleteConfirmationPopUp.run {
                    textLabel.setText("Are you sure you want to delete the level #$levelId?")
                    uiStage.addActor(this)
                    yesClickRunnable = Runnable {
                        if (getMapFactory(levelFilePath).id == levelEntity.map.levelId) {
                            isCurrentLevelDeleted = true
                        }
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

    private fun getPaddingFromName(paddingName: String) = when (paddingName) {
        "left" -> levelEntity.map.paddingLeft.toInt()
        "right" -> levelEntity.map.paddingRight.toInt()
        "top" -> levelEntity.map.paddingTop.toInt()
        "bottom" -> levelEntity.map.paddingBottom.toInt()
        else -> throw IllegalArgumentException("No padding found for the name given..")
    }

    private fun setPaddingFromName(paddingName: String, value: Int) {
        when (paddingName) {
            "left" -> levelEntity.map.paddingLeft = value.toFloat()
            "right" -> levelEntity.map.paddingRight = value.toFloat()
            "top" -> levelEntity.map.paddingTop = value.toFloat()
            "bottom" -> levelEntity.map.paddingBottom = value.toFloat()
            else -> throw java.lang.IllegalArgumentException("No padding found for the name given.")
        }
    }

    private fun createMinusPaddingPlus(paddingName: String) = Table(skin).apply {
        val paddingValueLabel =
            DistanceFieldLabel("${getPaddingFromName(paddingName)}", skin, "bold", 65f, Colors.gameColor)
        val minusButton = ClickButton(skin, "small-round-button").apply {
            addIcon("small-minus-icon")
            setColors(Colors.gameColor, Colors.uiDownColor)
            addClickRunnable(Runnable {
                val newPadding = getPaddingFromName(paddingName) - 1
                setPaddingFromName(paddingName, newPadding)
                paddingValueLabel.setText("$newPadding")
            })
        }
        val plusButton = ClickButton(skin, "small-round-button").apply {
            addIcon("small-plus-icon")
            setColors(Colors.gameColor, Colors.uiDownColor)
            addClickRunnable(Runnable {
                val newPadding = getPaddingFromName(paddingName) + 1
                setPaddingFromName(paddingName, newPadding)
                paddingValueLabel.setText("$newPadding")
            })
        }
        add(minusButton).space(15f)
        add(paddingValueLabel).space(15f)
        add(plusButton).space(15f)
    }

    private fun createCameraPopUpRightColumn() = Table(skin).apply {
        defaults().spaceBottom(20f)
        add(createMinusPaddingPlus("left")).growX().expand().row()
        add(createMinusPaddingPlus("right")).growX().expand().row()
        add(createMinusPaddingPlus("top")).growX().expand().row()
        add(createMinusPaddingPlus("bottom")).growX().expand().bottom()
    }

    private fun createCameraPopUp() = PopUp(590f, 530f, skin).apply {
        val title = DistanceFieldLabel("Padding", skin, "bold", 70f, Colors.gameColor)
        widget.run {
            pad(40f)
            add(title).top().colspan(2).row()
            add(cameraPopUpLeftColumn).left().padRight(28f)
            add(createCameraPopUpRightColumn()).grow().right()
        }
    }

    private fun createLoadLevelPopUp() = PopUp(520f, 500f, skin).apply {
        val scrollPane = ScrollPane(createLoadLevelTable(430f)).apply {
            setupOverscroll(50f, 80f, 200f)
        }
        widget.add(scrollPane).grow().top()
    }

    private fun createUI() {
        uiStage.addActor(rootTable)
        handleUIInput()
    }

    private fun handleUIInput() {
        // [index] is 0 so UI input is handled first, otherwise the buttons can't be pressed
        inputMultiplexer.addProcessor(0, uiStage)
    }

    private fun handleAllInput() {
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun update(delta: Float) {
        uiStage.act()
        engine.update(delta)
        gameStage.camera.update()
        if (!screenIsHidden) {
            updateUndoRedoButtonsColor()
        }
        updatePopUps()
    }

    /** Updates pop-ups in the game loop to avoid recursion problems. */
    private fun updatePopUps() {
        if (updateLoadLevelPopUp) {
            updateLoadLevelPopUp = false
            loadLevelPopUp = createLoadLevelPopUp()
            uiStage.addActor(loadLevelPopUp)
        }
        if (hideSettingsPopUp) {
            hideSettingsPopUp = false
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
            setColors(Colors.gameColor, Colors.gameColor)
            opaqueImage?.color?.a = 0f
            color.a = .3f
        }
    }

    private fun resetButtonColor(button: ClickButton) {
        button.run {
            setColors(Colors.gameColor, Colors.uiDownColor)
            opaqueImage?.color?.a = 1f
            color.a = 1f
        }
    }

    override fun render(delta: Float) {
        clearScreen(Colors.bgColor)
        update(delta)
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, true)
    }

    override fun hide() {
        screenIsHidden = true
    }
}