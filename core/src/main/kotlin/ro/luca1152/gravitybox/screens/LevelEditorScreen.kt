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
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import ktx.app.KtxScreen
import ktx.collections.contains
import ktx.inject.Context
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.editor.MockMapObjectComponent
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LevelEditorScreen(private val context: Context) : KtxScreen {
    // Injected objects
    private val engine: PooledEngine = context.inject()
    private val manager: AssetManager = context.inject()
    private val gameStage: GameStage = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val overlayViewport: OverlayViewport = context.inject()
    private val gameCamera: GameCamera = context.inject()
    private val uiStage: UIStage = context.inject()
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private val game: MyGame = context.inject()
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
    val placeToolButton = PaneButton(skin, "small-button").apply paneButton@{
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
            addIcon("collectible-point-icon")
            setColors(Colors.gameColor, Colors.uiDownColor)
            setOpaque(true)
            addClickRunnable(
                createButtonFromPaneRunnable(this@paneButton, this, CollectiblePointComponent::class.java)
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
        context,
        520f, 400f,
        "Are you sure you want to go back to the main menu?",
        skin, "regular", 50f,
        Colors.gameColor, yesIsHighlighted = true
    )
    private val saveBeforeLeavingPopUp = YesNoTextPopUp(
        context,
        550f, 350f,
        "Do you want to save the current level?",
        skin, "regular", 50f,
        Colors.gameColor, yesIsHighlighted = true
    ).apply {
        yesClickRunnable = Runnable {
            levelEntity.map.saveMap()
            game.setScreen(TransitionScreen(context, PlayScreen::class.java))
        }
        noClickRunnable = Runnable {
            game.setScreen(TransitionScreen(context, PlayScreen::class.java))
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
                        game.setScreen(TransitionScreen(context, PlayScreen::class.java))
                    }
                }
            } else {
                Runnable {
                    levelEntity.map.saveMap()
                    game.setScreen(TransitionScreen(context, PlayScreen::class.java))
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
            engine.addSystem(PlayingSystem(context, this@LevelEditorScreen))
        })
        setOpaque(true)
    }
    private val saveConfirmationPopUp = YesNoTextPopUp(
        context,
        520f, 400f,
        "Are you sure you want to save the level?",
        skin, "regular", 50f,
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
        context,
        450f, 250f,
        "Level saved successfully.",
        skin, "regular", 50f, Colors.gameColor
    )
    private val deleteConfirmationPopUp = YesNoTextPopUp(
        context,
        520f, 400f,
        "Are you sure you want to delete the level #[x]?",
        skin, "regular", 50f,
        Colors.gameColor, yesIsHighlighted = true
    )
    private val loadConfirmationPopUp = YesNoTextPopUp(
        context,
        520f, 400f,
        "Are you sure you want to load the level #[x]?",
        skin, "regular", 50f,
        Colors.gameColor, yesIsHighlighted = true
    )
    private var loadLevelPopUp = PopUp(context, 0f, 0f, skin)
    private val newLevelConfirmationPopUp = YesNoTextPopUp(
        context,
        520f, 400f,
        "Are you sure you want to create a new level?",
        skin, "regular", 50f,
        Colors.gameColor, yesIsHighlighted = true
    ).apply {
        yesClickRunnable = Runnable {
            if (isEditingNewLevel) {
                uiStage.addActor(saveLevelBeforeCreationConfirmationPopUp)
            } else {
                if (!isCurrentLevelDeleted) {
                    levelEntity.map.saveMap()
                }
                isEditingNewLevel = true
                isCurrentLevelDeleted = false
                updateLevelIdToFirstUnused()
                resetMapToInitialState()
            }
        }
    }
    private val saveLevelBeforeCreationConfirmationPopUp = YesNoTextPopUp(
        context,
        550f, 350f,
        "Do you want to save the current level?",
        skin, "regular", 50f,
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
        val left = DistanceFieldLabel(context, "Left", skin, "regular", 65f, Colors.gameColor)
        val right = DistanceFieldLabel(context, "Right", skin, "regular", 65f, Colors.gameColor)
        val top = DistanceFieldLabel(context, "Top", skin, "regular", 65f, Colors.gameColor)
        val bottom = DistanceFieldLabel(context, "Bottom", skin, "regular", 65f, Colors.gameColor)
        defaults().padTop(7f).padBottom(7f)
        add(left).row()
        add(right).row()
        add(top).row()
        add(bottom)
    }
    private val newButton = ClickTextButton(context, "simple-button", skin, "New", "regular", 75f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            uiStage.addActor(newLevelConfirmationPopUp)
        }
    }
    private val saveButton = ClickTextButton(context, "simple-button", skin, "Save", "regular", 75f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            uiStage.addActor(saveConfirmationPopUp)
        }
    }
    private val loadButton = ClickTextButton(context, "simple-button", skin, "Load", "regular", 75f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            loadLevelPopUp = createLoadLevelPopUp()
            uiStage.addActor(loadLevelPopUp)
        }
    }
    private val cameraButton = ClickTextButton(context, "simple-button", skin, "Camera", "regular", 75f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            uiStage.addActor(createCameraPopUp())
        }
    }
    private val playerButton = ClickTextButton(context, "simple-button", skin, "Player", "regular", 75f).apply {
        upColor = Colors.gameColor
        downColor = Colors.uiDownColor
        clickRunnable = Runnable {
            gameCamera.position.set(playerEntity.scene2D.centerX, playerEntity.scene2D.centerY, 0f)
            hideSettingsPopUp = true
        }
    }
    private val settingsPopUp = PopUp(context, 450f, 510f, skin).apply {
        widget.run {
            val buttonsTable = Table(skin).apply {
                defaults().padTop(-5f).padBottom(-5f)
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
        context.register { if (!contains<Skin>()) bindSingleton(skin) }
    }

    private fun createGameEntities() {
        inputEntity = InputEntity.createEntity(context, toggledButton)
        undoRedoEntity = UndoRedoEntity.createEntity(context)
        levelEntity = LevelEntity.createEntity(context, getFirstUnusedLevelId())
        finishEntity = FinishEntity.createEntity(context, blinkEndlessly = false)
        playerEntity = PlayerEntity.createEntity(context)
    }

    private fun loadLastEditedLevel() {
        val lastEditedMapFile = getLastEditedMapFile()
        val mapFactory = getMapFactory(lastEditedMapFile.path())
        levelEntity.map.loadMap(context, mapFactory, playerEntity, finishEntity, true)
        levelEntity.level.levelId = levelEntity.map.levelId
        isEditingNewLevel = false
        centerCameraOnPlayer()
    }

    private fun resetMapToInitialState() {
        removeAdditionalEntities()
        undoRedoEntity.undoRedo.reset()
        val platformEntity = PlatformEntity.createEntity(context, 0f, .5f, 4f)
        repositionDefaultEntities(platformEntity)
        centerCameraOnPlatform(platformEntity)
        settingsPopUp.remove()
    }

    private fun removeAdditionalEntities() {
        val entitiesToRemove = Array<Entity>()
        engine.getEntitiesFor(
            Family.one(
                MapObjectComponent::class.java, DashedLineComponent::class.java, MockMapObjectComponent::class.java
            ).exclude(
                PlayerComponent::class.java, FinishComponent::class.java
            ).get()
        ).forEach { entitiesToRemove.add(it) }
        entitiesToRemove.forEach { engine.removeEntity(it) }
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
            val formatter = SimpleDateFormat("yyyy-MM-dd HHmmss z", Locale.getDefault())
            try {
                val levelDate = formatter.parse(it.nameWithoutExtension())
                val currentDate = Date(TimeUtils.millis())
                val diffInMills = Math.abs(currentDate.time - levelDate.time)
                if (diffInMills < minLastEditedTime) {
                    minLastEditedTime = diffInMills
                    minLastEditedFile = it
                }
            } catch (e: ParseException) {
                if (Gdx.files.local("maps/editor").list().size == 1) {
                    return it
                }
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
        val oldId = levelEntity.map.levelId
        levelEntity.run {
            map.run {
                reset()
                updateMapBounds()
                levelId = oldId
            }
            level.run {
                levelId = oldId
                forceUpdateMap = true
            }
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
            addProcessor(context.inject<OverlayStage>())
            addProcessor(gameStage)
        }
    }

    fun addGameSystems() {
        engine.run {
            addSystem(FlushPreferencesSystem(context))
            addSystem(PlayTimeSystem(context))
            addSystem(UndoRedoSystem(context))
            addSystem(SelectedObjectColorSystem())
            addSystem(ObjectPlacementSystem(context, this@LevelEditorScreen))
            addSystem(TapThroughObjectsSystem(context))
            addSystem(ZoomingSystem(context))
            addSystem(PanningSystem(context))
            addSystem(ObjectSelectionSystem(context))
            addSystem(UpdateGameCameraSystem(context))
            addSystem(OverlayCameraSyncSystem(context))
            addSystem(ExtendedTouchSyncSystem())
            addSystem(GridRenderingSystem(context))
            addSystem(ObjectSnappingSystem(context))
            addSystem(OverlayPositioningSystem(context))
            addSystem(RoundedPlatformsSystem(context))
            addSystem(RotatingIndicatorSystem())
            addSystem(ColorSyncSystem())
            addSystem(DashedLineRenderingSystem(context))
            addSystem(FadeOutFadeInSystem(context))
            addSystem(ImageRenderingSystem(context))
            addSystem(OverlayRenderingSystem(context))
            addSystem(DebugRenderingSystem(context))
//            addSystem(PhysicsDebugRenderingSystem())
        }
    }

    private fun getLastEditedString(fileNameWithoutExtension: String): String {
        try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HHmmss z", Locale.getDefault())
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
        } catch (e: ParseException) {
            return "ERROR ago"
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
            context,
            "Level #${mapFactory.id}", skin, "regular",
            57f, Colors.gameColor
        )
        val lastEditedLabel = DistanceFieldLabel(
            context,
            lastEditedString, skin, "regular",
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
                        levelEntity.map.loadMap(context, mapFactory, playerEntity, finishEntity, true)
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
            DistanceFieldLabel(context, "${getPaddingFromName(paddingName)}", skin, "regular", 65f, Colors.gameColor)
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
        defaults().padBottom(-10f)
        add(createMinusPaddingPlus("left")).growX().expand().row()
        add(createMinusPaddingPlus("right")).growX().expand().row()
        add(createMinusPaddingPlus("top")).growX().expand().row()
        add(createMinusPaddingPlus("bottom")).growX().expand().bottom()
    }

    private fun createCameraPopUp() = PopUp(context, 590f, 530f, skin).apply {
        val title = DistanceFieldLabel(context, "Padding", skin, "regular", 70f, Colors.gameColor)
        widget.run {
            pad(40f)
            add(title).top().colspan(2).row()
            add(cameraPopUpLeftColumn).left().padRight(28f).padTop(5f)
            add(createCameraPopUpRightColumn()).padBottom(10f).padTop(10f).grow().right()
        }
    }

    private fun createLoadLevelPopUp() = PopUp(context, 520f, 500f, skin).apply {
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
            if (!button.isPressed) {
                color = Colors.gameColor
                icon?.color = Colors.gameColor
            }
            opaqueImage?.color = Colors.bgColor
            opaqueImage?.color?.a = 0f
            color.a = .3f
        }
    }

    private fun resetButtonColor(button: ClickButton) {
        button.run {
            setColors(Colors.gameColor, Colors.uiDownColor)
            if (!button.isPressed) {
                color = Colors.gameColor
                icon?.color = Colors.gameColor
            }
            opaqueImage?.color = Colors.bgColor
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
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, false)
        overlayViewport.update(width, height, false)
    }

    override fun hide() {
        screenIsHidden = true
    }
}