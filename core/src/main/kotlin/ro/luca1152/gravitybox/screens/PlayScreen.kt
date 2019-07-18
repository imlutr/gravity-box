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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.pay.*
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.app.KtxScreen
import ktx.inject.Context
import pl.mk5.gdx.fireapp.GdxFIRAnalytics
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.components.game.pixelsToMeters
import ro.luca1152.gravitybox.components.game.player
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.LevelEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.ads.AdsController
import ro.luca1152.gravitybox.utils.ads.RewardedAdEventListener
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.WorldContactListener
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.label.OutlineDistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.panes.*
import ro.luca1152.gravitybox.utils.ui.playscreen.*
import kotlin.math.min

@Suppress("ConstantConditionIf")
class PlayScreen(private val context: Context) : KtxScreen {
    // Injected objects
    private val manager: AssetManager = context.inject()
    private val engine: PooledEngine = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val world: World = context.inject()
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private val uiStage: UIStage = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val overlayViewport: OverlayViewport = context.inject()
    private val gameStage: GameStage = context.inject()
    private val eventQueue: EventQueue = context.inject()
    private val gameRules: GameRules = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val purchaseManager: PurchaseManager? = context.injectNullable()
    private val adsController: AdsController? = context.injectNullable()
    private val skin: Skin = context.inject()
    private val finishUiStage = Stage(context.inject<UIViewport>(), context.inject())

    // Bind this PlayScreen singleton so the following declarations don't cause a NullPointerException
    init {
        context.bindSingleton(this)
        initializePurchaseManager()
        initializeRewardedAds()
    }

    // Entities
    private lateinit var playerEntity: Entity
    lateinit var levelEntity: Entity

    // Constants
    val padTopBottom = 38f
    val padLeftRight = 43f

    // Variables
    var shiftCameraYBy = 0f

    // Listeners
    private val backKeyListener = BackKeyListener(context)
    private val clearPreferencesListener = ClearPreferencesListener(context)

    // Overlays
    val menuOverlay = MenuOverlay(context)

    // Panes
    val exitGameConfirmationPane = ExitGameConfirmationPane(context)
    val rateGamePromptPane = RateGamePromptPane(context) { menuOverlay.makeHeartButtonFull() }
    val heartPane = RateGameHeartPane(context) { menuOverlay.makeHeartButtonFull() }
    val gitHubPane = GitHubPane(context)
    val skipLevelPane = SkipLevelPane(context) { skipLevel() }
    val levelEditorPane = LevelEditorPane(context)
    private val restoreSuccessPane = RestoreSuccessPane(context)
    private val restoreNoPurchasesErrorPane = RestoreNoPurchasesErrorPane(context)
    private val restoreErrorPane = RestoreErrorPane(context)
    private val purchaseErrorPane = PurchaseErrorPane(context)
    private val purchaseSuccessPane = PurchaseSuccessPane(context)

    // Buttons
    val restartButton = RestartButton(context)
    val skipLevelButton = SkipLevelButton(context)
    val noAdsButton = NoAdsButton(context)
    val leaderboardButton = LeaderboardButton(context)
    val menuButton = MenuButton(context)

    val rankLabel = OutlineDistanceFieldLabel(
        context,
        "rank #x",
        skin, "regular", 37f, Colors.gameColor
    ).apply {
        isVisible = false
    }
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

    private val framedRestartButton = object : Image(skin.getDrawable("framed-restart-button")) {
        init {
            color = Colors.gameColor.apply { a = 0f }
            touchable = Touchable.disabled
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    if (!levelEntity.level.isRestarting) {
                        levelEntity.level.restartLevel = true
                        gameRules.RESTART_COUNT++
                    }
                    return true
                }
            })
        }

        override fun act(delta: Float) {
            super.act(delta)
            color.setWithoutAlpha(Colors.gameColor)
        }
    }

    private val levelFinishRankLabel = OutlineDistanceFieldLabel(
        context,
        "rank #x",
        skin, "semi-bold", 40f, Colors.gameColor
    ).apply {
        color.a = 0f
    }

    private val levelFinishRankPercentageLabel = OutlineDistanceFieldLabel(
        context,
        "(top x.y%)",
        skin, "regular", 30f, Colors.gameColor
    ).apply {
        color.a = 0f
    }

    private val levelFinishGuideLabel = OutlineDistanceFieldLabel(
        context,
        "Tap anywhere to proceed",
        skin, "regular", 37f, Colors.gameColor
    ).apply {
        color.a = 0f
    }

    private val levelFinishTouchableTransparentImage = Image(manager.get(Assets.tileset).findRegion("pixel")).apply {
        width = uiViewport.worldWidth
        height = uiViewport.worldHeight
        touchable = Touchable.disabled
        color = Color.CLEAR
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                eventQueue.add(ShowNextLevelEvent())
                return true
            }
        })
    }

    private val rootFinishUiTable = Table().apply {
        setFillParent(true)
        addActor(levelFinishTouchableTransparentImage)
        add(levelFinishRankLabel).top().padTop(69f).row()
        add(levelFinishRankPercentageLabel).top().row()
        add(levelFinishGuideLabel).top().padTop(30f).expand().row()
        add(framedRestartButton).expand().bottom().row()
    }

    private fun initializePurchaseManager() {
        if (!gameRules.IS_MOBILE)
            return

        val purchaseObserver = object : PurchaseObserver {
            override fun handleInstall() {
                // Restore purchases every time the game launches, thus if the game is reinstalled, the game remains ad-free (if a purchase was made)
                // Do this only on Android, as Apple forbids restoring purchases without any user interaction
                // => on iOS there will be an "I already paid..." button
                if (gameRules.IS_ANDROID) {
                    purchaseManager!!.purchaseRestore()
                }
            }

            override fun handleInstallError(e: Throwable?) {}

            override fun handleRestore(transactions: Array<out Transaction>) {
                if (transactions.isEmpty() && gameRules.IS_IOS) {
                    menuOverlayStage.addActor(restoreNoPurchasesErrorPane)
                } else {
                    transactions.forEach {
                        handleTransaction(it)
                    }

                    // Silently restore on Android
                    if (gameRules.IS_IOS) {
                        menuOverlayStage.addActor(restoreSuccessPane)
                    }
                }
            }

            override fun handleRestoreError(e: Throwable?) {
                // Silently handle restore errors on Android
                if (gameRules.IS_IOS) {
                    menuOverlayStage.addActor(restoreErrorPane)
                }
            }

            override fun handlePurchase(transaction: Transaction) {
                handleTransaction(transaction)
                menuOverlayStage.addActor(purchaseSuccessPane)
            }

            private fun handleTransaction(transaction: Transaction) {
                if (transaction.isPurchased && transaction.affectsAds()) {
                    gameRules.IS_AD_FREE = true
                }
            }

            override fun handlePurchaseError(e: Throwable?) {
                menuOverlayStage.addActor(purchaseErrorPane)
            }

            override fun handlePurchaseCanceled() {}

            private fun Transaction.affectsAds() =
                identifier == "coffee" || identifier == "ice_cream" || identifier == "muffin" || identifier == "pizza" || identifier == "sushi"
        }
        val purchaseManagerConfig = PurchaseManagerConfig().apply {
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("coffee"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("ice_cream"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("muffin"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("pizza"))
            addOffer(Offer().setType(OfferType.CONSUMABLE).setIdentifier("sushi"))
        }
        purchaseManager!!.install(purchaseObserver, purchaseManagerConfig, true)
    }

    private fun layoutTables() {
        // Fixes issue #55 (UI bug)
        menuOverlay.rootTable.layout()
        rootTable.layout()
        rootFinishUiTable.layout()
    }

    private fun initializeRewardedAds() {
        adsController?.rewardedAdEventListener = object : RewardedAdEventListener {
            override fun onRewardedEvent(type: String, amount: Int) {
                Gdx.app.log("AdMob", "Rewarding player with [$type, $amount].")
                gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN = gameRules.TIME_DELAY_BETWEEN_REWARDED_ADS
                skipLevel()
            }

            override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
                menuOverlayStage.addActor(RewardedAddErrorPane(context, errorCode))
            }
        }
    }

    override fun show() {
        layoutTables()
        createGame()
        createUI()
    }

    private fun createGame() {
        setOwnBox2DContactListener()
        createGameEntities()
        PlayScreenSystems(context).add()
        handleAllInput()
    }

    private fun setOwnBox2DContactListener() {
        world.setContactListener(WorldContactListener(context))
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
    }

    private fun handleAllInput() {
        Gdx.input.inputProcessor = inputMultiplexer
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
            addActor(rootFinishUiTable)
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

    private var loadedAnyMap = false
    private var isSkippingLevel = false
    private fun skipLevel() {
        if (levelEntity.level.levelId == gameRules.LEVEL_COUNT) {
            return
        }

        if (gameRules.IS_MOBILE) {
            GdxFIRAnalytics.inst().logEvent("skip_level", mapOf(Pair("level_id", "game/${levelEntity.level.levelId}")))
        }

        gameRules.run {
            if (getGameLevelHighscore(levelEntity.level.levelId) == gameRules.DEFAULT_HIGHSCORE_VALUE)
                setGameLevelHighscore(levelEntity.level.levelId, gameRules.SKIPPED_LEVEL_SCORE_VALUE)
            HIGHEST_FINISHED_LEVEL = levelEntity.level.levelId
        }
        levelEntity.level.levelId++
        isSkippingLevel = true

        val fadeOutDuration = .2f
        val fadeInDuration = .2f
        gameStage.addAction(
            Actions.sequence(
                Actions.run {
                    levelEntity.level.isChangingLevel = true
                    eventQueue.add(FadeOutEvent(fadeOutDuration))
                },
                Actions.delay(fadeOutDuration),
                Actions.run {
                    menuOverlay.shouldUpdateLevelLabel = true
                    levelEntity.level.run {
                        loadMap = true
                        forceUpdateMap = true
                    }
                    levelEntity.map.run {
                        eventQueue.add(UpdateRoundedPlatformsEvent())
                        forceCenterCameraOnPlayer = true
                    }
                },
                Actions.run { eventQueue.add(FadeInEvent(fadeInDuration)) },
                Actions.delay(fadeInDuration),
                Actions.run {
                    levelEntity.level.isChangingLevel = false
                    isSkippingLevel = false
                }
            )
        )

        gameRules.SKIPPED_LEVELS_COUNT++
    }

    override fun render(delta: Float) {
        update()
        draw(delta)
        loadedAnyMap = true
        menuOverlay.rootTable.setLayoutEnabled(false)
    }

    private fun update() {
        updateMenuButtonTouchability()
        updateRankLabel()
        updateFinishRankLabel()
        updateFinishRankPercentageLabel()
        updateSkipLevelButton()
        updateUiAfterLevelFinish()
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

    private fun updateMenuButtonTouchability() {
        menuButton.touchable = if (playerEntity.player.isInsideFinishPoint) Touchable.disabled else Touchable.enabled
    }

    private fun updateRankLabel() {
        if (levelEntity.level.isLevelFinished) return

        if (levelEntity.map.rank == -1) {
            rankLabel.isVisible = false
        } else {
            rankLabel.isVisible = true
            rankLabel.run {
                setText("rank #${levelEntity.map.rank}")
                layout()
            }
        }
    }

    private fun updateFinishRankLabel() {
        if (!levelEntity.level.isLevelFinished) return
        if (levelEntity.map.rank != -1) {
            levelFinishRankLabel.run {
                setText("rank #${levelEntity.map.rank}")
                layout()
            }
        }
    }

    private fun updateFinishRankPercentageLabel() {
        if (!levelEntity.level.isLevelFinished) return
        levelFinishRankPercentageLabel.run {
            val percentageAsString = "%.1f".format(levelEntity.map.rankPercentage)
            setText("(top ${if (percentageAsString == "0.0") "0.1" else percentageAsString}%)")
            layout()
        }
    }

    private fun updateSkipLevelButton() {
        if (levelEntity.level.isLevelFinished) return

        // The skip level button should be hidden if the current level is not the highest finished one
        skipLevelButton.run {
            if (levelEntity.level.levelId != gameRules.HIGHEST_FINISHED_LEVEL + 1 && !isSkippingLevel &&
                // On Android it takes a bit to update Preferences, thus the skip level button flashed a bit without this condition
                levelEntity.level.levelId != gameRules.HIGHEST_FINISHED_LEVEL + 2
            ) {
                touchable = Touchable.disabled
                if (!hasActions() && color.a == 1f) {
                    addAction(Actions.fadeOut(.2f))
                }
            } else if (!isTouchable || isSkippingLevel) {
                touchable = Touchable.enabled
                if (!hasActions() && color.a == 0f) {
                    addAction(Actions.fadeIn(.2f))
                }
            }

            // The skip level button should be hidden if the current level is the last one.
            if (levelEntity.level.levelId == gameRules.LEVEL_COUNT) {
                color.a = 0f
                touchable = Touchable.disabled
                if (skipLevelPane.stage != null) {
                    skipLevelPane.hide()
                }
            }
        }
    }

    private fun updateUiAfterLevelFinish() {
        if (!levelEntity.level.isLevelFinished || levelEntity.level.isRestarting) return

        // The leaderboard was not loaded yet, so the finish UI shouldn't be shown
        if (context.injectNullable<GameShotsLeaderboard>() == null) return

        // The finish UI was already shown
        if (framedRestartButton.isTouchable) return

        // Hide menu overlay
        if (menuOverlay.bottomGrayStrip.y == 0f) {
            menuOverlay.hideMenuOverlay()
        }

        // Fade out things
        val fadeOutDuration = .2f
        skipLevelButton.run {
            touchable = Touchable.disabled
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        noAdsButton.run {
            touchable = Touchable.disabled
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        menuButton.run {
            touchable = Touchable.disabled
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        leaderboardButton.run {
            touchable = Touchable.disabled
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        restartButton.run {
            touchable = Touchable.disabled
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        rankLabel.run {
            addAction(Actions.fadeOut(fadeOutDuration))
        }

        // Fade in things
        val fadeInDuration = .2f
        framedRestartButton.run {
            touchable = Touchable.enabled
            addAction(
                Actions.sequence(
                    Actions.delay(fadeOutDuration),
                    Actions.fadeIn(fadeInDuration)
                )
            )
        }
        levelFinishRankLabel.run {
            addAction(
                Actions.sequence(
                    Actions.delay(fadeOutDuration),
                    Actions.fadeIn(fadeInDuration)
                )
            )
        }
        levelFinishRankPercentageLabel.run {
            addAction(
                Actions.sequence(
                    Actions.delay(fadeOutDuration),
                    Actions.fadeIn(fadeInDuration)
                )
            )
        }
        levelFinishGuideLabel.run {
            if (!gameRules.DID_SHOW_GUIDE_BETWEEN_LEVELS) {
                addAction(
                    Actions.sequence(
                        Actions.delay(fadeOutDuration),
                        Actions.fadeIn(fadeInDuration)
                    )
                )
            }
        }
        levelFinishTouchableTransparentImage.run {
            addAction(
                Actions.sequence(
                    Actions.delay(fadeOutDuration),
                    Actions.run {
                        levelFinishTouchableTransparentImage.touchable = Touchable.enabled
                    }
                )
            )
        }
    }

    fun hideLevelFinishUi() {
        // The finish UI was already hidden
        if (!framedRestartButton.isTouchable) return

        // Fade out things
        val fadeOutDuration = .2f
        framedRestartButton.run {
            touchable = Touchable.disabled
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        levelFinishRankLabel.run {
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        levelFinishRankPercentageLabel.run {
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        levelFinishGuideLabel.run {
            addAction(Actions.fadeOut(fadeOutDuration))
        }
        levelFinishTouchableTransparentImage.touchable = Touchable.disabled

        // Fade in things
        val fadeInDuration = .2f
        skipLevelButton.run {
            if (!((levelEntity.level.levelId != gameRules.HIGHEST_FINISHED_LEVEL + 1 && !isSkippingLevel &&
                        // On Android it takes a bit to update Preferences, thus the skip level button flashed a bit without this condition
                        levelEntity.level.levelId != gameRules.HIGHEST_FINISHED_LEVEL + 2))
            ) {
                touchable = Touchable.enabled
                clearActions()
                addAction(
                    Actions.sequence(
                        Actions.delay(fadeOutDuration),
                        Actions.fadeIn(fadeInDuration)
                    )
                )
            }
        }
        noAdsButton.run {
            touchable = Touchable.enabled
            addAction(Actions.sequence(Actions.delay(fadeOutDuration), Actions.fadeIn(fadeInDuration)))
        }
        menuButton.run {
            touchable = Touchable.enabled
            addAction(Actions.sequence(Actions.delay(fadeOutDuration), Actions.fadeIn(fadeInDuration)))
        }
        leaderboardButton.run {
            touchable = Touchable.enabled
            addAction(Actions.sequence(Actions.delay(fadeOutDuration), Actions.fadeIn(fadeInDuration)))
        }
        restartButton.run {
            touchable = Touchable.enabled
            addAction(Actions.sequence(Actions.delay(fadeOutDuration), Actions.fadeIn(fadeInDuration)))
        }
        rankLabel.run {
            color.a = 1f
            isVisible = false
        }
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