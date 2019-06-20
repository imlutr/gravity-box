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
import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.pay.*
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.app.KtxScreen
import ktx.graphics.copy
import ktx.inject.Context
import ktx.log.info
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.components.game.pixelsToMeters
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.entities.game.LevelEntity
import ro.luca1152.gravitybox.entities.game.PlayerEntity
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.FadeInEvent
import ro.luca1152.gravitybox.events.FadeOutEvent
import ro.luca1152.gravitybox.events.UpdateRoundedPlatformsEvent
import ro.luca1152.gravitybox.systems.editor.DashedLineRenderingSystem
import ro.luca1152.gravitybox.systems.editor.SelectedObjectColorSystem
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.ads.AdsController
import ro.luca1152.gravitybox.utils.ads.RewardedAdEventListener
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.WorldContactListener
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import ro.luca1152.gravitybox.utils.ui.popup.NewPopUp

@Suppress("ConstantConditionIf")
class PlayScreen(private val context: Context) : KtxScreen {
    // Injected objects
    private val manager: AssetManager = context.inject()
    private val game: MyGame = context.inject()
    private val engine: PooledEngine = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val world: World = context.inject()
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private val uiStage: UIStage = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val overlayViewport: OverlayViewport = context.inject()
    private val gameStage: GameStage = context.inject()
    private val preferences: Preferences = context.inject()
    private val eventQueue: EventQueue = context.inject()
    private val gameRules: GameRules = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val purchaseManager: PurchaseManager? = context.injectNullable()
    private val adsController: AdsController? = context.injectNullable()

    // Entities
    private lateinit var levelEntity: Entity

    private val padTopBottom = 38f
    private val padLeftRight = 43f
    private val bottomGrayStripHeight = 128f
    private val skin = manager.get(Assets.uiSkin)
    var shiftCameraYBy = 0f
    var shouldUpdateLevelLabel = false
    private val exitGameConfirmationPopUp = NewPopUp(context, 600f, 370f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            Are you sure you want to quit
            the game?
            """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val exitButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(
                context,
                "Exit",
                skin, "regular", 36f, Color.WHITE
            )
            add(buttonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    Gdx.app.exit()
                }
            })
        }
        val keepPlayingButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(
                context,
                "Keep playing",
                skin, "regular", 36f, Color.WHITE
            )
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(33f).expand().top().row()
            add(exitButton).width(492f).padBottom(32f).row()
            add(keepPlayingButton).width(492f).row()
        }
    }
    private val backKeyListener = object : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.BACK) {
                val popUp = menuOverlayStage.root.findActor<NewPopUp>("NewPopUp")
                if (popUp != null && !popUp.hasActions()) {
                    popUp.backButtonRunnable.run()
                } else {
                    when {
                        bottomGrayStrip.y == 0f -> hideMenuOverlay()
                        exitGameConfirmationPopUp.stage == null -> {
                            menuOverlayStage.addActor(exitGameConfirmationPopUp)
                            exitGameConfirmationPopUp.toFront()
                        }
                    }
                }
                return true
            }
            return false
        }
    }
    private val clearPreferencesListener = object : InputAdapter() {
        override fun keyDown(keycode: Int): Boolean {
            if (keycode == Input.Keys.F5 && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                preferences.run {
                    clear()
                    flush()
                }
                info { "Cleared all preferences." }
                return true
            }
            return false
        }
    }
    private val menuButton = ClickButton(skin, "menu-button").apply {
        addClickRunnable(Runnable {
            addAction(Actions.sequence(
                Actions.run {
                    touchable = Touchable.disabled
                    skipLevelButton.run {
                        addAction(
                            Actions.moveTo(uiStage.viewport.worldWidth, y, .2f, Interpolation.pow3In)
                        )
                    }
                    restartButton.run {
                        addAction(
                            Actions.moveTo(uiStage.viewport.worldWidth, y, .2f, Interpolation.pow3In)
                        )
                    }
                },
                Actions.delay(.1f),
                Actions.parallel(
                    Actions.moveTo(x, bottomGrayStripHeight, .2f, Interpolation.pow3In),
                    Actions.fadeOut(.2f, Interpolation.pow3In),
                    Actions.run {
                        showMenuOverlay()
                    }
                )
            ))
        })
    }
    private val restartButton = ClickButton(skin, "color-round-button").apply {
        addIcon("restart-icon")
        addClickRunnable(Runnable {
            if (!levelEntity.level.isRestarting) {
                levelEntity.level.restartLevel = true
                gameRules.RESTART_COUNT++
            }
        })
    }
    private val skipLevelButton = ClickButton(skin, "color-round-button").apply {
        addIcon("skip-level-icon")
        iconCell!!.padLeft(6f) // The icon doesn't SEEM centered
        addClickRunnable(Runnable {
            menuOverlayStage.addActor(skipLevelPopUp)
        })
    }
    private val skipLevelPopUp = object : NewPopUp(context, 600f, 370f, skin) {
        val thisPopUp = this // Can't put a popup@... So this will do
        val skipLevelButtonText = DistanceFieldLabel(context, "Skip level", skin, "regular", 36f, Color.WHITE)
        val skipLevelButton = Button(skin, "long-button").apply {
            add(skipLevelButtonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    hide()

                    if (gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN > 0f) {
                        return
                    }

                    // Ad-free
                    if (gameRules.IS_AD_FREE) {
                        gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN = gameRules.TIME_DELAY_BETWEEN_REWARDED_ADS
                        skipLevel()
                        return
                    }

                    // Debug
                    if (!gameRules.IS_MOBILE || adsController == null) {
                        gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN = gameRules.TIME_DELAY_BETWEEN_REWARDED_ADS
                        skipLevel()
                        return
                    }

                    if (!adsController.isNetworkConnected()) {
                        menuOverlayStage.addActor(noInternetRewardedVideoPopUp)
                    } else {
                        // Instantly hide the pop-up
                        thisPopUp.run {
                            clearActions()
                            remove()
                        }
                        adsController.showRewardedAd()
                    }
                }
            })
        }

        init {
            val text = DistanceFieldLabel(
                context,
                """
                Watch a short video to skip
                this level?
                """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
            )
            val noThanksButton = Button(skin, "long-button").apply {
                val buttonText = DistanceFieldLabel(context, "No, thanks", skin, "regular", 36f, Color.WHITE)
                add(buttonText)
                color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        super.clicked(event, x, y)
                        hide()
                    }
                })
            }
            widget.run {
                add(text).padBottom(32f).row()
                add(skipLevelButton).width(492f).padBottom(32f).row()
                add(noThanksButton).width(492f).row()
            }
        }

        override fun act(delta: Float) {
            super.act(delta)
            if (gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN <= 0f) {
                skipLevelButtonText.setText("Skip level")
                skipLevelButton.run {
                    setColor(0 / 255f, 129 / 255f, 213 / 255f, 1f)
                }
            } else {
                skipLevelButtonText.setText("Wait ${secondsToTimeString(gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN)}")
                skipLevelButton.run {
                    setColor(99 / 255f, 116 / 255f, 132 / 255f, 1f)
                }
            }
        }

        private fun secondsToTimeString(seconds: Float): String {
            val convertedHours = MathUtils.floor(seconds / 3600f)
            val convertedMinutes = MathUtils.floor((seconds % 3600) / 60f)
            val convertedSeconds = MathUtils.floor(seconds % 60)
            return (if (convertedHours == 0) "" else if (convertedHours <= 9) "0$convertedHours:" else "$convertedHours}:") +
                    (if (convertedMinutes == 0) "00:" else if (convertedMinutes <= 9) "0$convertedMinutes:" else "$convertedMinutes") +
                    (if (convertedSeconds == 0) "00" else if (convertedSeconds <= 9) "0$convertedSeconds" else "$convertedSeconds")
        }
    }
    private val noInternetRewardedVideoPopUp = NewPopUp(context, 600f, 334f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            Couldn't load rewarded video...

            Please make sure you are
            connected to the internet.""".trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val okayButton = Button(skin, "long-button").apply {
            val closeButton = DistanceFieldLabel(context, "Okay :(", skin, "regular", 36f, Color.WHITE)
            add(closeButton)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(okayButton).width(492f).row()
        }
    }
    private val bottomRow = Table().apply {
        add(menuButton).expand().padLeft(restartButton.prefWidth)
        add(restartButton).right()
    }
    private val rootTable = Table().apply {
        setFillParent(true)
        padLeft(padLeftRight).padRight(padLeftRight)
        padBottom(padTopBottom).padTop(padTopBottom)
        add(skipLevelButton).expand().top().right().row()
        add(bottomRow).expand().fillX().bottom()
    }
    private val githubPopUp = NewPopUp(context, 600f, 440f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            This game is fully open-source!

            Maybe star the GitHub repository
            if you like the project. <3
            """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val visitGithubButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(
                context,
                "Visit GitHub repository",
                skin, "regular", 36f, Color.WHITE
            )
            add(buttonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    Gdx.net.openURI("https://github.com/Luca1152/gravity-box")
                    this@popup.hide()
                }
            })
        }
        val maybeLaterButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(
                context,
                "Maybe later",
                skin, "regular", 36f, Color.WHITE
            )
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(33f).expand().top().row()
            add(visitGithubButton).width(492f).padBottom(32f).row()
            add(maybeLaterButton).width(492f).row()
        }
    }
    private val githubButton = ClickButton(skin, "gray-full-round-button").apply {
        addIcon("github-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(githubPopUp)
            }
        })
    }
    private val levelEditorPopUp = NewPopUp(context, 600f, 370f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            Do you want to go to the level
            editor?
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val yesButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Go to the level editor", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    game.setScreen(TransitionScreen(context, LevelEditorScreen::class.java, false))
                }
            })
        }
        val maybeLaterButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Maybe later", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(yesButton).width(492f).padBottom(32f).row()
            add(maybeLaterButton).width(492f).row()
        }
    }
    private val levelEditorButton = ClickButton(skin, "gray-full-round-button").apply {
        addIcon("level-editor-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(levelEditorPopUp)
            }
        })
    }
    private val topPartImage = object : Image(manager.get(Assets.tileset).findRegion("pixel")) {
        init {
            width = uiViewport.worldWidth
            height = uiViewport.worldHeight
            color = Color.WHITE.copy(alpha = 0f)
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    return if (bottomGrayStrip.y != 0f) false else super.touchDown(event, x, y, pointer, button)
                }

                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    hideMenuOverlay()
                }
            })
        }

        override fun act(delta: Float) {
            super.act(delta)
            if (color.a != 0f && !hasActions()) {
                color.setWithoutAlpha(Colors.bgColor)
            }
        }
    }
    private val leftButton = ClickButton(skin, "left-button").apply {
        touchable = Touchable.disabled
        addClickRunnable(Runnable {
            if (color.a == 1f && !levelEntity.level.isChangingLevel) {
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
                            shouldUpdateLevelLabel = true
                            levelEntity.level.run {
                                levelId = if (levelId == 1) {
                                    if (gameRules.CAN_PLAY_ANY_LEVEL) gameRules.LEVEL_COUNT
                                    else gameRules.HIGHEST_FINISHED_LEVEL + 1
                                } else levelId - 1
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
                        Actions.run { levelEntity.level.isChangingLevel = false }
                    )
                )
            }
        })
    }
    private val rightButton = ClickButton(skin, "right-button").apply {
        touchable = Touchable.disabled
        addClickRunnable(Runnable {
            if (color.a == 1f && !levelEntity.level.isChangingLevel) {
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
                            shouldUpdateLevelLabel = true
                            levelEntity.level.run {
                                levelId =
                                    if (levelId == gameRules.LEVEL_COUNT && gameRules.CAN_PLAY_ANY_LEVEL) 1
                                    else if (levelId == gameRules.HIGHEST_FINISHED_LEVEL + 1 && !gameRules.CAN_PLAY_ANY_LEVEL) 1
                                    else levelId + 1
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
                        Actions.run { levelEntity.level.isChangingLevel = false }
                    )
                )
            }
        })
    }
    private val levelLabel = DistanceFieldLabel(
        context,
        "#${when {
            gameRules.PLAY_SPECIFIC_LEVEL != -1 -> gameRules.PLAY_SPECIFIC_LEVEL
            gameRules.CAN_PLAY_ANY_LEVEL -> 1
            else -> Math.min(
                gameRules.HIGHEST_FINISHED_LEVEL + 1,
                gameRules.LEVEL_COUNT
            )
        }}",
        skin, "semi-bold", 37f, Colors.gameColor
    ).apply {
        addListener(object : ClickListener() {
            // Make the player not shoot if the label is clicked
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                return true
            }
        })
    }
    private val leftLevelRightTable = Table(skin).apply {
        add(leftButton).padRight(64f)
        add(levelLabel).padRight(64f)
        add(rightButton)
        addAction(Actions.fadeOut(0f))
    }
    private val topTable = Table(skin).apply {
        if (gameRules.ENABLE_LEVEL_EDITOR) {
            add(levelEditorButton).expand().top().left().padLeft(-levelEditorButton.prefWidth)
        }
        add(githubButton).expand().top().right().padRight(-githubButton.prefWidth)
    }
    private val topPart = Table(skin).apply {
        addActor(topPartImage)
        add(topTable).grow().top().padTop(padTopBottom).row()
        add(leftLevelRightTable).expand().bottom()
    }
    private val heartPopUp = NewPopUp(context, 600f, 440f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            Would you like to rate the game
            or give feedback?

            (I actually read every review)
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val rateButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Rate the game", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    when (Gdx.app.type) {
                        Application.ApplicationType.Android -> Gdx.net.openURI("market://details?id=ro.luca1152.gravitybox")
                        else -> Gdx.net.openURI("https://play.google.com/store/apps/details?id=ro.luca1152.gravitybox")
                    }
                    gameRules.DID_RATE_THE_GAME = true
                    makeHeartButtonFull()
                    this@popup.hide()
                }
            })
        }
        val maybeLaterButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Maybe later", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).expand().padBottom(32f).row()
            add(rateButton).width(492f).padBottom(32f).row()
            add(maybeLaterButton).width(492f).row()
        }
    }
    private val heartButton = ClickButton(
        skin,
        if (gameRules.DID_RATE_THE_GAME) "white-full-round-button" else "empty-round-button"
    ).apply {
        addIcon("heart-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(heartPopUp)
            }
        })
    }
    private val audioButton = ClickButton(skin, "white-full-round-button").apply {
        val order = arrayListOf("sounds-and-music-icon", "music-icon", "sounds-icon", "no-sounds-icon")
        var current = order.first()
        fun getNextIcon() =
            if (order.indexOf(current) == order.size - 1) order.first() else order[order.indexOf(current) + 1]
        addIcon(current)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                current = getNextIcon()

                icon!!.remove()
                iconCell!!.reset()
                addIcon(current)
                layout()

                styleName = if (current == "no-sounds-icon") "empty-round-button" else "white-full-round-button"
                style = skin.get(styleName, Button.ButtonStyle::class.java)
            }
        })
    }

    private fun createNoAdsPopUp() = NewPopUp(
        context, 600f,
        if (!gameRules.IS_AD_FREE) {
            if (gameRules.IS_IOS) 924f // Show the "I already paid..." button
            else 820f // Hide the "I already paid..." button
        } else 856f, skin
    ).apply popup@{
        val text = DistanceFieldLabel(
            context,
            (if (!gameRules.IS_AD_FREE)
                """
                Any amount below will support
                the development & REMOVE
                ADS! <3
                """
            else
                """
                The game is now ad-free!

                Any amount below will support
                the development! <3
                """).trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val coffeeButton = Button(skin, "long-button").apply {
            val coffeeText = DistanceFieldLabel(context, "Coffee", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel(context, "$1.99", skin, "regular", 36f, Color.WHITE)
            add(coffeeText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(0 / 255f, 190 / 255f, 214 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    purchaseManager?.purchase("coffee")
                    this@popup.hide()

                    // Debug
                    if (!gameRules.IS_MOBILE) {
                        gameRules.IS_AD_FREE = true
                    }
                    return true
                }
            })
        }
        val iceCreamButton = Button(skin, "long-button").apply {
            val iceCreamText = DistanceFieldLabel(context, "Ice Cream (best)", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel(context, "$4.99", skin, "regular", 36f, Color.WHITE)
            add(iceCreamText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(207 / 255f, 0 / 255f, 214 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    purchaseManager?.purchase("ice_cream")
                    this@popup.hide()

                    // Debug
                    if (!gameRules.IS_MOBILE) {
                        gameRules.IS_AD_FREE = true
                    }
                    return true
                }
            })
        }
        val muffinButton = Button(skin, "long-button").apply {
            val muffinText = DistanceFieldLabel(context, "Muffin", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel(context, "$7.99", skin, "regular", 36f, Color.WHITE)
            add(muffinText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(24 / 255f, 178 / 255f, 230 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    purchaseManager?.purchase("muffin")
                    this@popup.hide()

                    // Debug
                    if (!gameRules.IS_MOBILE) {
                        gameRules.IS_AD_FREE = true
                    }
                    return true
                }
            })
        }
        val pizzaButton = Button(skin, "long-button").apply {
            val pizzaText = DistanceFieldLabel(context, "Pizza", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel(context, "$14.99", skin, "regular", 36f, Color.WHITE)
            add(pizzaText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(24 / 255f, 154 / 255f, 230 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    purchaseManager?.purchase("pizza")
                    this@popup.hide()

                    // Debug
                    if (!gameRules.IS_MOBILE) {
                        gameRules.IS_AD_FREE = true
                    }
                    return true
                }
            })
        }
        val sushiButton = Button(skin, "long-button").apply {
            val sushiText = DistanceFieldLabel(context, "Sushi", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel(context, "$24.99", skin, "regular", 36f, Color.WHITE)
            add(sushiText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(0 / 255f, 125 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    purchaseManager?.purchase("sushi")
                    this@popup.hide()

                    // Debug
                    if (!gameRules.IS_MOBILE) {
                        gameRules.IS_AD_FREE = true
                    }
                    return true
                }
            })
        }
        val alreadyPaidButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "I already paid...", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    purchaseManager?.purchaseRestore()
                    this@popup.hide()
                }
            })
        }
        val noThanksButton = Button(skin, "long-button").apply {
            val buttonText =
                DistanceFieldLabel(context, "No, thanks${if (!gameRules.IS_AD_FREE) " :(" else ""}", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(coffeeButton).width(492f).padBottom(32f).row()
            add(iceCreamButton).width(492f).padBottom(32f).row()
            add(muffinButton).width(492f).padBottom(32f).row()
            add(pizzaButton).width(492f).padBottom(32f).row()
            add(sushiButton).width(492f).padBottom(32f).row()
            if (!gameRules.IS_AD_FREE && gameRules.IS_IOS) {
                add(alreadyPaidButton).width(492f).padBottom(32f).row()
            }
            add(noThanksButton).width(492f).row()
        }
    }

    private val noAdsButton = ClickButton(skin, "empty-round-button").apply {
        addIcon("no-ads-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(createNoAdsPopUp())
            }
        })
    }
    private val bottomGrayStrip = Table(skin).apply {
        val grayImage = object : Image(manager.get(Assets.tileset).findRegion("pixel")) {
            init {
                width = uiStage.viewport.worldWidth
                height = bottomGrayStripHeight
                color = Colors.uiGray
                addListener(object : ClickListener() {
                    override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                        return true
                    }
                })
            }

            override fun act(delta: Float) {
                super.act(delta)
                color = Colors.uiGray
            }
        }
        addActor(grayImage)
        val leftPart = Table(skin).apply {
            add(heartButton)
        }
        // For now the game has no audio
        @Suppress("UNUSED_VARIABLE") val middlePart = Table(skin).apply {
            add(audioButton)
        }
        val rightPart = Table(skin).apply {
            add(noAdsButton)
        }
        add(leftPart).padLeft(padLeftRight).expand().left()
//        add(middlePart).expand()
        add(rightPart).padRight(padLeftRight).expand().right()
    }
    val rateGamePromptPopUp = NewPopUp(context, 600f, 570f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            Would you like to rate the game
            or give feedback?

            (I actually read every review)
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val rateButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Rate the game", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    when (Gdx.app.type) {
                        Application.ApplicationType.Android -> Gdx.net.openURI("market://details?id=ro.luca1152.gravitybox")
                        else -> Gdx.net.openURI("https://play.google.com/store/apps/details?id=ro.luca1152.gravitybox")
                    }
                    gameRules.DID_RATE_THE_GAME = true
                    makeHeartButtonFull()
                    this@popup.hide()
                }
            })
        }
        val maybeLaterButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Maybe later", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    gameRules.MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN =
                        gameRules.PLAY_TIME + gameRules.TIME_DELAY_BETWEEN_PROMPTING_USER_TO_RATE_THE_GAME_AGAIN
                    this@popup.hide()
                }
            })
        }
        val neverButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Never :(", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    gameRules.NEVER_PROMPT_USER_TO_RATE_THE_GAME = true
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(rateButton).width(492f).padBottom(32f).row()
            add(maybeLaterButton).width(492f).padBottom(32f).row()
            add(neverButton).width(492f).row()
        }
        backButtonRunnable = Runnable {
            hide()

            // Prompt the player to rate the game later
            gameRules.MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN =
                gameRules.PLAY_TIME + gameRules.TIME_DELAY_BETWEEN_PROMPTING_USER_TO_RATE_THE_GAME_AGAIN
        }
    }
    private val anErrorOccurredRestorePopUp = NewPopUp(context, 600f, 370f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            An error occurred while
            restoring purchases....

            Please make sure you are
            connected to the internet.""".trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val okayButton = Button(skin, "long-button").apply {
            val closeButton = DistanceFieldLabel(context, "Okay :(", skin, "regular", 36f, Color.WHITE)
            add(closeButton)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(okayButton).width(492f).row()
        }
    }
    private val anErrorOccurredPurchasePopUp = NewPopUp(context, 600f, 260f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            An error occurred while
            purchasing....""".trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val okayButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Okay :(", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(okayButton).width(492f).row()
        }
    }
    private val successfulRestorePopUp = NewPopUp(context, 600f, 230f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            "Purchases successfully restored.", skin, "regular", 36f, skin.getColor("text-gold")
        )
        val okayButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Okay :)", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(okayButton).width(492f).row()
        }
    }
    private val noPurchasesToRestorePopUp = NewPopUp(context, 600f, 230f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            "No purchases to restore...", skin, "regular", 36f, skin.getColor("text-gold")
        )
        val okayButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Okay :(", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(okayButton).width(492f).row()
        }
    }
    private val successfulPurchasePopUp = NewPopUp(context, 600f, 340f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            Successful purchase!

            Thanks for supporting the
            game's development! <3
            """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val okayButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(context, "Close :)", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(okayButton).width(492f).row()
        }
    }
    private val rootOverlayTable = Table().apply {
        setFillParent(true)
        add(topPart).expand().fill().row()
        add(bottomGrayStrip).fillX().height(bottomGrayStripHeight).bottom().padBottom(-128f)
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
                    menuOverlayStage.addActor(noPurchasesToRestorePopUp)
                } else {
                    transactions.forEach {
                        handleTransaction(it)
                    }

                    // Silently restore on Android
                    if (gameRules.IS_IOS) {
                        menuOverlayStage.addActor(successfulRestorePopUp)
                    }
                }
            }

            override fun handleRestoreError(e: Throwable?) {
                // Silently handle restore errors on Android
                if (gameRules.IS_IOS) {
                    menuOverlayStage.addActor(anErrorOccurredRestorePopUp)
                }
            }

            override fun handlePurchase(transaction: Transaction) {
                handleTransaction(transaction)
                menuOverlayStage.addActor(successfulPurchasePopUp)
            }

            private fun handleTransaction(transaction: Transaction) {
                if (transaction.isPurchased && transaction.affectsAds()) {
                    gameRules.IS_AD_FREE = true
                }
            }

            override fun handlePurchaseError(e: Throwable?) {
                menuOverlayStage.addActor(anErrorOccurredPurchasePopUp)
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

    private fun showMenuOverlay() {
        githubButton.run {
            addAction(Actions.moveTo(x - padLeftRight - prefWidth, y, .2f, Interpolation.pow3In))
        }
        levelEditorButton.run {
            addAction(Actions.moveTo(x + padLeftRight + prefWidth, y, .2f, Interpolation.pow3In))
        }
        bottomGrayStrip.run {
            addAction(Actions.moveTo(0f, 0f, .2f, Interpolation.pow3In))
        }
        topPartImage.touchable = Touchable.enabled
        topPartImage.addAction(
            Actions.parallel(
                Actions.moveTo(0f, bottomGrayStripHeight, .2f, Interpolation.pow3In),
                Actions.color(Colors.bgColor.copy(alpha = .4f), .3f)
            )
        )
        leftLevelRightTable.run {
            addAction(
                Actions.sequence(
                    Actions.parallel(
                        Actions.moveTo(x, y + 100f + bottomGrayStripHeight, .2f, Interpolation.pow3In),
                        Actions.fadeIn(.2f, Interpolation.pow3In)
                    ),
                    Actions.run {
                        leftButton.touchable = Touchable.enabled
                        rightButton.touchable = Touchable.enabled
                    }
                )
            )
        }
    }

    private fun hideMenuOverlay() {
        githubButton.run {
            addAction(Actions.moveTo(x + padLeftRight + prefWidth, y, .2f, Interpolation.pow3In))
        }
        levelEditorButton.run {
            addAction(Actions.moveTo(x - padLeftRight - prefWidth, y, .2f, Interpolation.pow3In))
        }
        bottomGrayStrip.run {
            addAction(
                Actions.sequence(
                    Actions.delay(.1f),
                    Actions.moveTo(0f, -bottomGrayStripHeight, .2f, Interpolation.pow3In)
                )
            )
        }
        topPartImage.touchable = Touchable.disabled
        topPartImage.addAction(
            Actions.sequence(
                Actions.delay(.1f),
                Actions.parallel(
                    Actions.moveTo(0f, 0f, .2f, Interpolation.pow3In),
                    Actions.color(Color.WHITE.copy(alpha = 0f), .3f)
                )
            )
        )
        skipLevelButton.run {
            addAction(
                Actions.sequence(
                    Actions.delay(.1f),
                    Actions.moveTo(
                        uiStage.viewport.worldWidth - padLeftRight - prefWidth,
                        y, .2f, Interpolation.pow3In
                    )
                )
            )
        }
        restartButton.run {
            addAction(
                Actions.sequence(
                    Actions.delay(.1f),
                    Actions.moveTo(
                        uiStage.viewport.worldWidth - 2 * padLeftRight - prefWidth,
                        y, .2f, Interpolation.pow3In
                    )
                )
            )
        }
        menuButton.addAction(
            Actions.sequence(
                Actions.delay(.1f),
                Actions.parallel(
                    Actions.moveTo(menuButton.x, 0f, .2f, Interpolation.pow3In),
                    Actions.fadeIn(.2f, Interpolation.pow3In)
                ),
                Actions.run {
                    menuButton.touchable = Touchable.enabled
                }
            )
        )
        leftLevelRightTable.run {
            addAction(
                Actions.sequence(
                    Actions.delay(.1f),
                    Actions.parallel(
                        Actions.moveTo(x, 0f, .2f, Interpolation.pow3In),
                        Actions.fadeOut(.2f, Interpolation.pow3In)
                    ),
                    Actions.run {
                        leftButton.touchable = Touchable.disabled
                        rightButton.touchable = Touchable.disabled
                    }
                )
            )
        }
    }

    init {
        layoutTables()
        initializePurchaseManager()
        initializeRewardedAds()
    }

    private fun layoutTables() {
        // Fixes issue #55 (UI bug)
        rootOverlayTable.layout()
        rootTable.layout()
    }

    private fun initializeRewardedAds() {
        adsController?.rewardedAdEventListener = object : RewardedAdEventListener {
            override fun onRewardedEvent(type: String, amount: Int) {
                Gdx.app.log("AdMob", "Rewarding player with [$type, $amount].")
                gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN = gameRules.TIME_DELAY_BETWEEN_REWARDED_ADS
                skipLevel()
            }

            override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
                menuOverlayStage.addActor(anErrorOccurredRewardedAd(errorCode))
            }
        }
    }

    private fun anErrorOccurredRewardedAd(errorCode: Int) = NewPopUp(context, 600f, 334f, skin).apply popup@{
        val text = DistanceFieldLabel(
            context,
            """
            An error occurred while loading
            the rewarded video...

            Error code $errorCode.""".trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val okayButton = Button(skin, "long-button").apply {
            val closeButton = DistanceFieldLabel(context, "Okay :(", skin, "regular", 36f, Color.WHITE)
            add(closeButton)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).padBottom(32f).row()
            add(okayButton).width(492f).row()
        }
    }

    override fun show() {
        context.register { if (!contains<Skin>()) bindSingleton(skin) }
        createGame()
        createUI()
    }

    private fun createGame() {
        setOwnBox2DContactListener()
        createGameEntities()
        addGameSystems()
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
                else -> Math.min(
                    gameRules.HIGHEST_FINISHED_LEVEL + 1,
                    gameRules.LEVEL_COUNT
                )
            }
        ).apply {
            level.loadMap = true
            level.forceUpdateMap = true
            map.forceCenterCameraOnPlayer = true
        }
        PlayerEntity.createEntity(context)
        FinishEntity.createEntity(context)
    }

    private fun addGameSystems() {
        engine.run {
            addSystem(FlushPreferencesSystem(context))
            addSystem(PlayTimeSystem(context))
            addSystem(RewardedAdTimerSystem(context))
            addSystem(InterstitialAdsSystem(context))
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
            addSystem(LevelFinishDetectionSystem())
            addSystem(PointsCollectionSystem(context))
            addSystem(LevelRestartSystem(context))
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
            addSystem(LevelFinishSystem(context, playScreen = this@PlayScreen))
            addSystem(ShowFinishStatsSystem(context))
//            addSystem(PhysicsDebugRenderingSystem(context))
            addSystem(DebugRenderingSystem(context))
        }
    }

    private fun handleAllInput() {
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun makeHeartButtonFull() {
        heartButton.run {
            styleName = "white-full-round-button"
            style = skin.get(styleName, Button.ButtonStyle::class.java)
        }
    }

    private fun createUI() {
        uiStage.run {
            clear()
            addActor(rootTable)
        }
        menuOverlayStage.run {
            clear()
            addActor(rootOverlayTable)
        }
        handleUiInput()
    }

    private fun handleUiInput() {
        // [index] is 0 so UI input is handled first, otherwise the buttons can't be pressed
        inputMultiplexer.addProcessor(0, menuOverlayStage)
        inputMultiplexer.addProcessor(1, uiStage)
        inputMultiplexer.addProcessor(2, clearPreferencesListener)

        // Back key
        inputMultiplexer.addProcessor(3, backKeyListener)
        Gdx.input.isCatchBackKey = true
    }

    private var loadedAnyMap = false
    private var isSkippingLevel = false
    private fun skipLevel() {
        if (levelEntity.level.levelId == gameRules.LEVEL_COUNT) {
            return
        }

        gameRules.HIGHEST_FINISHED_LEVEL = levelEntity.level.levelId
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
                    shouldUpdateLevelLabel = true
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
        rootOverlayTable.setLayoutEnabled(false)
    }

    private fun update() {
        updateLevelLabel()
        updateSkipLevelButton()
        updateLeftRightButtons()
        shiftCameraYBy = (bottomGrayStrip.y + 128f).pixelsToMeters
        uiStage.act()
        menuOverlayStage.act()
    }

    private fun draw(delta: Float) {
        clearScreen(if (loadedAnyMap) Colors.bgColor else Color.BLACK)
        engine.update(delta)
        uiStage.draw()
        menuOverlayStage.draw()
    }

    private fun updateLevelLabel() {
        if (shouldUpdateLevelLabel) {
            levelLabel.run {
                setText("#${levelEntity.level.levelId}")
                layout()
            }
            rootOverlayTable.setLayoutEnabled(false)
            shouldUpdateLevelLabel = false
        }
    }

    private fun updateSkipLevelButton() {
        // The skip level button should be hidden if the current level is not the highest finished one
        skipLevelButton.run {
            if (levelEntity.level.levelId != gameRules.HIGHEST_FINISHED_LEVEL + 1 && !isSkippingLevel &&
                // On Android it takes a bit to update Preferences, thus the skip level button flashed a bit without this condition
                levelEntity.level.levelId != gameRules.HIGHEST_FINISHED_LEVEL + 2
            ) {
                color.a = 0f
                touchable = Touchable.disabled
            } else if (!isTouchable || isSkippingLevel) {
                color.a = 1f
                touchable = Touchable.enabled
            }

            // The skip level button should be hidden if the current level is the last one.
            if (levelEntity.level.levelId == gameRules.LEVEL_COUNT) {
                color.a = 0f
                touchable = Touchable.disabled
                if (skipLevelPopUp.stage != null) {
                    skipLevelPopUp.hide()
                }
            }
        }
    }

    private fun updateLeftRightButtons() {
        leftButton.run {
            styleName = if (levelEntity.level.levelId == 1) "double-left-button" else "left-button"
            style = skin.get(styleName, Button.ButtonStyle::class.java)
        }
        rightButton.run {
            styleName =
                if ((gameRules.CAN_PLAY_ANY_LEVEL && levelEntity.level.levelId == gameRules.LEVEL_COUNT) ||
                    (!gameRules.CAN_PLAY_ANY_LEVEL && levelEntity.level.levelId == gameRules.HIGHEST_FINISHED_LEVEL + 1)
                ) "double-right-button"
                else "right-button"
            style = skin.get(styleName, Button.ButtonStyle::class.java)
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