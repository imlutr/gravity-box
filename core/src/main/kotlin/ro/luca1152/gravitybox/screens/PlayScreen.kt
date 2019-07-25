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
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.app.KtxScreen
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.components.game.pixelsToMeters
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.LevelEntity
import ro.luca1152.gravitybox.entities.game.NetworkEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.systems.editor.DashedLineRenderingSystem
import ro.luca1152.gravitybox.systems.editor.SelectedObjectColorSystem
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.ads.MyRewardedAds
import ro.luca1152.gravitybox.utils.box2d.WorldContactListener
import ro.luca1152.gravitybox.utils.iap.MyPurchaseManager
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.label.OutlineDistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.panes.*
import ro.luca1152.gravitybox.utils.ui.playscreen.*
import kotlin.math.min

@Suppress("ConstantConditionIf")
class PlayScreen(private val context: Context) : KtxScreen {
    private val engine: PooledEngine = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val world: World = context.inject()
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private val uiStage: UIStage = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val overlayViewport: OverlayViewport = context.inject()
    private val gameRules: GameRules = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val skin: Skin = context.inject()
    private val finishUiStage = Stage(context.inject<UIViewport>(), context.inject())

    // Bind this PlayScreen singleton so the following declarations don't cause a NullPointerException
    init {
        context.bindSingleton(this)
        MyPurchaseManager(context).init()
        MyRewardedAds(context).init()
    }

    // Entities
    lateinit var playerEntity: Entity
    lateinit var levelEntity: Entity
    lateinit var networkEntity: Entity

    // Constants
    val padTopBottom = 38f
    val padLeftRight = 43f

    // Variables
    private var loadedAnyMap = false
    var shiftCameraYBy = 0f

    // Listeners
    private val backKeyListener = BackKeyListener(context)
    private val clearPreferencesListener = ClearPreferencesListener(context)

    // Overlays
    val finishOverlay = FinishOverlay(context)
    val menuOverlay = MenuOverlay(context)

    // Panes
    val exitGameConfirmationPane = ExitGameConfirmationPane(context)
    val rateGamePromptPane = RateGamePromptPane(context) { menuOverlay.makeHeartButtonFull() }
    val heartPane = RateGameHeartPane(context) { menuOverlay.makeHeartButtonFull() }
    val gitHubPane = GitHubPane(context)
    val skipLevelPane = SkipLevelPane(context)
    val levelEditorPane = LevelEditorPane(context)

    // Buttons
    val restartButton = RestartButton(context)
    val skipLevelButton = SkipLevelButton(context)
    val noAdsButton = NoAdsButton(context)
    val leaderboardButton = LeaderboardButton(context)
    val menuButton = MenuButton(context)

    // Labels
    val rankLabel = object : OutlineDistanceFieldLabel(
        context, "rank #x",
        skin, "regular", 37f, Colors.gameColor
    ) {
        private val rankTexts = (1..200).associateWith { "rank #$it" }

        init {
            isVisible = false
        }

        override fun act(delta: Float) {
            super.act(delta)
            updateLabel()
        }

        private fun updateLabel() {
            if (levelEntity.level.isLevelFinished) return

            if (levelEntity.map.rank == -1 || levelEntity.level.isRestarting) {
                isVisible = false
            } else {
                isVisible = true
                val rank = levelEntity.map.rank
                setText(if (rankTexts.containsKey(rank)) rankTexts.getValue(rank) else "rank #$rank")
                layout()
            }
        }
    }

    // Tables
    private val topRow = Table().apply {
        add(skipLevelButton).padRight(6f)
        add(noAdsButton).expand().left()
        add(menuButton).expand()
        add(leaderboardButton).expand().right()
        add(restartButton).padLeft(6f)
    }
    private val rootTable = Table().apply {
        setFillParent(true)
        padLeft(padLeftRight).padRight(padLeftRight)
        padBottom(padTopBottom).padTop(padTopBottom)
        add(topRow).fillX().expandY().top().padTop(-25f).padLeft(-25f).padRight(-25f).row()
        add(rankLabel).expand().bottom().padBottom(31f).row()
    }

    override fun show() {
        layoutTables()
        createGame()
        createUI()
    }

    private fun layoutTables() {
        // Fixes issue #55 (UI bug)
        rootTable.layout()
        menuOverlay.rootTable.layout()
        finishOverlay.rootTable.layout()
    }

    private fun createGame() {
        world.setContactListener(WorldContactListener(context))
        createGameEntities()
        addGameSystems()
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun createGameEntities() {
        levelEntity = LevelEntity.createEntity(
            context,
            when {
                gameRules.PLAY_SPECIFIC_LEVEL != -1 -> gameRules.PLAY_SPECIFIC_LEVEL
                gameRules.CAN_PLAY_ANY_LEVEL -> 1
                else -> min(gameRules.HIGHEST_FINISHED_LEVEL + 1, gameRules.LEVEL_COUNT)
            }
        ).apply {
            level.loadMap = true
            level.forceUpdateMap = true
            map.forceCenterCameraOnPlayer = true
        }
        playerEntity = PlayerEntity.createEntity(context)
        FinishEntity.createEntity(context)
        networkEntity = NetworkEntity.createEntity(context)
    }

    private fun addGameSystems() {
        engine.run {
            addSystem(NetworkDetectionSystem(context))
            addSystem(SkipLevelSystem(context))
            addSystem(EntireLeaderboardCachingSystem(context))
            addSystem(WriteEntireLeaderboardToStorageSystem(context))
            addSystem(LeaderboardRankCalculationSystem(context))
            addSystem(UpdateAllRanksSystem(context))
            addSystem(CurrentLevelShotsCachingSystem(context))
            addSystem(FlushPreferencesIntervalSystem(context))
            addSystem(FlushPreferencesSystem(context))
            addSystem(PlayTimeSystem(context))
            addSystem(RewardedAdTimerSystem(context))
            addSystem(InterstitialAdsSystem(context))
            addSystem(LevelPlayTimeLoggingSystem(context))
            addSystem(GameFinishSystem(context))
            addSystem(MapLoadingSystem(context))
            addSystem(MapBodiesCreationSystem(context))
            addSystem(CombinedBodiesCreationSystem(context))
            addSystem(RoundedPlatformsSystem(context))
            addSystem(PhysicsSystem(context))
            addSystem(ObjectMovementSystem())
            addSystem(RefilterSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(ShootingSystem(context))
            addSystem(BulletCollisionSystem(context))
            addSystem(PlatformRemovalSystem(context))
            addSystem(OffScreenLevelRestartSystem(context))
            addSystem(OffScreenBulletDeletionSystem(context))
            addSystem(KeyboardLevelRestartSystem(context))
            addSystem(PlayerInsideFinishDetectionSystem())
            addSystem(FinishTimingSystem())
            addSystem(LevelFinishDetectionSystem())
            addSystem(PointsCollectionSystem(context))
            addSystem(LevelRestartSystem(context, this@PlayScreen))
            addSystem(CanFinishLevelSystem(context))
            addSystem(FinishPointColorSystem())
            addSystem(ColorSchemeSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem(context, this@PlayScreen))
            addSystem(UpdateGameCameraSystem(context))
            addSystem(DashedLineRenderingSystem(context))
            addSystem(FadeOutFadeInSystem(context))
            addSystem(ImageRenderingSystem(context))
            addSystem(CheatingCheckingSystem(context))
            addSystem(LevelFinishSystem(context))
            addSystem(WriteRankToStorageSystem(context))
            addSystem(ShowNextLevelSystem(context, this@PlayScreen))
            addSystem(PlaySpecificLevelSystem(context))
            addSystem(PromptUserToRateSystem(context, this@PlayScreen))
            addSystem(ShowInterstitialAdSystem(context))
            addSystem(ShowFinishStatsSystem(context))
//            addSystem(PhysicsDebugRenderingSystem(context))
            addSystem(DebugRenderingSystem(context))
        }
    }

    private fun createUI() {
        uiStage.run {
            clear()
            addActor(rootTable)
        }
        menuOverlayStage.run {
            clear()
            addActor(menuOverlay.rootTable)
        }
        finishUiStage.run {
            clear()
            addActor(finishOverlay.rootTable)
        }
        handleUiInput()
    }

    private fun handleUiInput() {
        // [index] is 0 so UI input is handled first, otherwise the buttons can't be pressed
        inputMultiplexer.addProcessor(0, menuOverlayStage)
        inputMultiplexer.addProcessor(1, uiStage)
        inputMultiplexer.addProcessor(2, finishUiStage)
        inputMultiplexer.addProcessor(3, clearPreferencesListener)

        // Back key
        inputMultiplexer.addProcessor(4, backKeyListener)
        Gdx.input.isCatchBackKey = true
    }

    override fun render(delta: Float) {
        update()
        draw(delta)
        loadedAnyMap = true
        menuOverlay.rootTable.setLayoutEnabled(false)
    }

    private fun update() {
        finishOverlay.update()
        shiftCameraYBy = (menuOverlay.bottomGrayStrip.y + 128f).pixelsToMeters
        uiStage.act()
        menuOverlayStage.act()
        finishUiStage.act()
    }

    private fun draw(delta: Float) {
        clearScreen(if (loadedAnyMap) Colors.bgColor else Color.BLACK)
        engine.update(delta)
        uiStage.draw()
        menuOverlayStage.draw()
        finishUiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, false)
        overlayViewport.update(width, height, false)
        menuOverlayStage.viewport.update(width, height, false)
    }

    override fun hide() {
        world.setContactListener(null)
        Gdx.input.inputProcessor = null
        engine.removeSystem(engine.getSystem(FinishPointColorSystem::class.java))
    }
}