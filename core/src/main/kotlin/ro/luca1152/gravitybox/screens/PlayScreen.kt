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
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.graphics.copy
import ktx.inject.Context
import ktx.log.info
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
import ro.luca1152.gravitybox.systems.editor.SelectedObjectColorSystem
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.WorldContactListener
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import ro.luca1152.gravitybox.utils.ui.popup.NewPopUp

class PlayScreen(private val context: Context) : KtxScreen {
    // Injected objects
    private val manager: AssetManager = context.inject()
    private val game: MyGame = context.inject()
    private val engine: PooledEngine = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val world: World = context.inject()
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private val uiStage: UIStage = context.inject()
    private val uiCamera: UICamera = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val overlayViewport: OverlayViewport = context.inject()
    private val gameStage: GameStage = context.inject()
    private val preferences: Preferences = context.inject()
    private val eventQueue: EventQueue = context.inject()

    // Entities
    private lateinit var levelEntity: Entity

    private val canLoadAnyLevel = true // debug
    private val cycleFromFirstToLastLevel = true

    private val menuOverlayStage = Stage(ExtendViewport(720f, 1280f, uiCamera), context.inject())
    private val padTopBottom = 38f
    private val padLeftRight = 43f
    private val bottomGrayStripHeight = 128f
    private val skin = manager.get(Assets.uiSkin)
    var shiftCameraYBy = 0f
    var shouldUpdateLevelLabel = false
    private var isChangingLevel = false
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
                    restartButton.addAction(
                        Actions.moveTo(uiStage.viewport.worldWidth, 0f, .2f, Interpolation.pow3In)
                    )
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
            }
        })
    }
    private val bottomRow = Table().apply {
        add(menuButton).expand().padLeft(restartButton.prefWidth)
        add(restartButton).right()
    }
    private val rootTable = Table().apply {
        setFillParent(true)
        padLeft(padLeftRight).padRight(padLeftRight)
        padBottom(padTopBottom).padTop(padTopBottom)
        add(bottomRow).expand().fillX().bottom()
    }
    private val githubPopUp = NewPopUp(context, 600f, 508f, skin).apply popup@{
        val text = DistanceFieldLabel(
            """
            This game is fully open-source!

            If you want to support the
            development, consider starring
            the GitHub repository, as the
            visibility would really help!
            """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val visitGithubButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel(
                "Visit repository on GitHub",
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
            add(maybeLaterButton).width(492f).expand().bottom().row()
        }
    }
    private val githubButton = ClickButton(skin, "gray-full-round-button").apply {
        addIcon("github-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                stage.addActor(githubPopUp)
            }
        })
    }
    private val levelEditorPopUp = NewPopUp(context, 600f, 400f, skin).apply popup@{
        val text = DistanceFieldLabel(
            """
            Do you want to go to the level
            editor?
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val yesButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel("Go to the level editor", skin, "regular", 36f, Color.WHITE)
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
            val buttonText = DistanceFieldLabel("Maybe later", skin, "regular", 36f, Color.WHITE)
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
            add(text).expand().row()
            add(yesButton).width(492f).expand().row()
            add(maybeLaterButton).width(492f).expand().row()
        }
    }
    private val levelEditorButton = ClickButton(skin, "gray-full-round-button").apply {
        addIcon("level-editor-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                stage.addActor(levelEditorPopUp)
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
        addClickRunnable(Runnable {
            // The button is touchable
            if (color.a == 1f && !isChangingLevel) {
                val fadeOutDuration = .2f
                val fadeInDuration = .2f
                gameStage.addAction(
                    Actions.sequence(
                        Actions.run {
                            isChangingLevel = true
                            eventQueue.add(FadeOutEvent(fadeOutDuration))
                        },
                        Actions.delay(fadeOutDuration),
                        Actions.run {
                            shouldUpdateLevelLabel = true
                            levelEntity.level.run {
                                if (levelId == 1) levelId = MyGame.LEVELS_NUMBER
                                else levelId--
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
                        Actions.run { isChangingLevel = false }
                    )
                )
            }
        })
    }
    private val rightButton = ClickButton(skin, "right-button").apply {
        addClickRunnable(Runnable {
            // The button is touchable
            if (color.a == 1f && !isChangingLevel) {
                val fadeOutDuration = .2f
                val fadeInDuration = .2f
                gameStage.addAction(
                    Actions.sequence(
                        Actions.run {
                            isChangingLevel = true
                            eventQueue.add(FadeOutEvent(fadeOutDuration))
                        },
                        Actions.delay(fadeOutDuration),
                        Actions.run {
                            shouldUpdateLevelLabel = true
                            levelEntity.level.run {
                                levelId++
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
                        Actions.run { isChangingLevel = false }
                    )
                )
            }
        })
    }
    private val levelLabel = DistanceFieldLabel(
        "#${if (canLoadAnyLevel) 1 else (Math.min(
            preferences.getInteger("highestFinishedLevel", 0) + 1,
            MyGame.LEVELS_NUMBER
        ))}",
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
        add(leftButton).padRight(102f)
        add(levelLabel).padRight(102f)
        add(rightButton)
        addAction(Actions.fadeOut(0f))
    }
    private val topTable = Table(skin).apply {
        add(levelEditorButton).expand().top().left().padLeft(-levelEditorButton.prefWidth)
        add(githubButton).expand().top().right().padRight(-githubButton.prefWidth)
    }
    private val topPart = Table(skin).apply {
        addActor(topPartImage)
        add(topTable).grow().top().padTop(padTopBottom).row()
        add(leftLevelRightTable).expand().bottom()
    }
    private val heartPopUp = NewPopUp(context, 600f, 450f, skin).apply popup@{
        val text = DistanceFieldLabel(
            """
            Would you like to rate the game
            or give feedback?

            (I actually read every review)
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val rateButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel("Rate the game", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    when (Gdx.app.type) {
                        Application.ApplicationType.Android -> Gdx.net.openURI("market://details?id=ro.luca1152.gravitybox")
                        else -> Gdx.net.openURI("https://play.google.com/store/apps/details?id=ro.luca1152.gravitybox")
                    }
                    preferences.run {
                        putBoolean("didRateGame", true)
                        flush()
                    }
                    makeHeartButtonFull()
                    this@popup.hide()
                }
            })
        }
        val maybeLaterButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel("Maybe later", skin, "regular", 36f, Color.WHITE)
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
            add(text).expand().top().row()
            add(rateButton).width(492f).expand().row()
            add(maybeLaterButton).width(492f).expand().bottom().row()
        }
    }
    private val heartButton = ClickButton(
        skin,
        if (preferences.getBoolean("didRateGame")) "white-full-round-button" else "empty-round-button"
    ).apply {
        addIcon("heart-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                stage.addActor(heartPopUp)
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
    private val leaderboardsButton = ClickButton(skin, "empty-round-button").apply {
        addIcon("leaderboards-icon")
    }
    private val noAdsPopUp = NewPopUp(context, 600f, 970f, skin).apply popup@{
        val text = DistanceFieldLabel(
            """
            This game is powered by a
            team of 1 (and open-source
            contributors)!

            Any amount below will support
            the development & REMOVE
            ADS! <3
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val coffeeButton = Button(skin, "long-button").apply {
            val coffeeText = DistanceFieldLabel("Coffee", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel("$1.99", skin, "regular", 36f, Color.WHITE)
            add(coffeeText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(0 / 255f, 190 / 255f, 214 / 255f, 1f)
        }
        val iceCreamButton = Button(skin, "long-button").apply {
            val iceCreamText = DistanceFieldLabel("Ice Cream (best)", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel("$4.99", skin, "regular", 36f, Color.WHITE)
            add(iceCreamText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(207 / 255f, 0 / 255f, 214 / 255f, 1f)
        }
        val muffinButton = Button(skin, "long-button").apply {
            val muffinText = DistanceFieldLabel("Muffin", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel("$7.49", skin, "regular", 36f, Color.WHITE)
            add(muffinText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(24 / 255f, 178 / 255f, 230 / 255f, 1f)
        }
        val pizzaButton = Button(skin, "long-button").apply {
            val pizzaText = DistanceFieldLabel("Pizza", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel("$12.49", skin, "regular", 36f, Color.WHITE)
            add(pizzaText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(24 / 255f, 154 / 255f, 230 / 255f, 1f)
        }
        val sushiButton = Button(skin, "long-button").apply {
            val sushiText = DistanceFieldLabel("Sushi", skin, "regular", 36f, Color.WHITE)
            val priceText = DistanceFieldLabel("$24.99", skin, "regular", 36f, Color.WHITE)
            add(sushiText).padLeft(47f).expand().left()
            add(priceText).padRight(47f).expand().right()
            color.set(0 / 255f, 125 / 255f, 213 / 255f, 1f)
        }
        val noThanksButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel("No Thanks :(", skin, "regular", 36f, Color.WHITE)
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
            add(text).expand().top().row()
            add(coffeeButton).width(492f).expand().row()
            add(iceCreamButton).width(492f).expand().row()
            add(muffinButton).width(492f).expand().row()
            add(pizzaButton).width(492f).expand().row()
            add(sushiButton).width(492f).expand().row()
            add(noThanksButton).width(492f).expand().row()
        }
    }
    private val noAdsButton = ClickButton(skin, "empty-round-button").apply {
        addIcon("no-ads-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                stage.addActor(noAdsPopUp)
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
        val middlePart = Table(skin).apply {
            add(audioButton).padRight(46f)
            add(leaderboardsButton)
        }
        val rightPart = Table(skin).apply {
            add(noAdsButton)
        }
        add(leftPart).padLeft(padLeftRight).expand().left()
        add(middlePart).expand()
        add(rightPart).padRight(padLeftRight).expand().right()
    }
    val rateGamePromptPopUp = NewPopUp(context, 600f, 570f, skin).apply popup@{
        val text = DistanceFieldLabel(
            """
            Would you like to rate the game
            or give feedback?

            (I actually read every review)
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
        )
        val rateButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel("Rate the game", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    when (Gdx.app.type) {
                        Application.ApplicationType.Android -> Gdx.net.openURI("market://details?id=ro.luca1152.gravitybox")
                        else -> Gdx.net.openURI("https://play.google.com/store/apps/details?id=ro.luca1152.gravitybox")
                    }
                    preferences.run {
                        putBoolean("didRateGame", true)
                        flush()
                    }
                    makeHeartButtonFull()
                    this@popup.hide()
                }
            })
        }
        val maybeLaterButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel("Maybe later", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    this@popup.hide()
                }
            })
        }
        val neverButton = Button(skin, "long-button").apply {
            val buttonText = DistanceFieldLabel("Never :(", skin, "regular", 36f, Color.WHITE)
            add(buttonText)
            color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    preferences.run {
                        putBoolean("neverPromptUserToRate", true)
                        flush()
                    }
                    this@popup.hide()
                }
            })
        }
        widget.run {
            add(text).expand().top().row()
            add(rateButton).width(492f).expand().row()
            add(maybeLaterButton).width(492f).expand().row()
            add(neverButton).width(492f).expand().row()
        }
    }
    private val rootOverlayTable = Table().apply {
        setFillParent(true)
        add(topPart).expand().fill().row()
        add(bottomGrayStrip).fillX().height(bottomGrayStripHeight).bottom().padBottom(-128f)
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
                    )
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
                    )
                )
            )
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
            if (canLoadAnyLevel) 1 else
                Math.min(
                    preferences.getInteger("highestFinishedLevel", 0) + 1,
                    MyGame.LEVELS_NUMBER
                )
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
            addSystem(PlatformRemovalSystem())
            addSystem(OffScreenLevelRestartSystem())
            addSystem(OffScreenBulletDeletionSystem(context))
            addSystem(KeyboardLevelRestartSystem(context))
            addSystem(LevelFinishDetectionSystem())
            addSystem(PointsCollectionSystem())
            addSystem(LevelRestartSystem(context))
            addSystem(FinishPointColorSystem())
            addSystem(ColorSchemeSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(ColorSyncSystem())
            addSystem(CanFinishLevelSystem(context))
            addSystem(PlayerCameraSystem(context, this@PlayScreen))
            addSystem(UpdateGameCameraSystem(context))
            addSystem(FadeOutFadeInSystem(context))
            addSystem(ImageRenderingSystem(context))
            addSystem(LevelFinishSystem(context, playScreen = this@PlayScreen))
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
        inputMultiplexer.addProcessor(0, uiStage)
        inputMultiplexer.addProcessor(1, menuOverlayStage)
        inputMultiplexer.addProcessor(2, clearPreferencesListener)
    }

    private var loadedAnyMap = false

    override fun render(delta: Float) {
        update()
        draw(delta)
        loadedAnyMap = true
        rootOverlayTable.setLayoutEnabled(false)
    }

    private fun update() {
        updateLeftRightButtons()
        updateLevelLabel()
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

    private fun updateLeftRightButtons() {
        if (levelEntity.level.levelId == 1 && !cycleFromFirstToLastLevel) {
            makeButtonUntouchable(leftButton)
        } else {
            makeButtonTouchable(leftButton)
        }

        if (levelEntity.level.levelId == if (canLoadAnyLevel) MyGame.LEVELS_NUMBER else Math.min(
                MyGame.LEVELS_NUMBER,
                preferences.getInteger("highestFinishedLevel", 0) + 1
            )
        ) {
            makeButtonUntouchable(rightButton)
        } else {
            makeButtonTouchable(rightButton)
        }
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

    private fun makeButtonUntouchable(button: ClickButton) {
        button.run {
            syncColorsWithColorScheme = false
            color.set(Colors.gameColor)
            color.a = .3f
            downColor = color
            upColor = color
        }
    }

    private fun makeButtonTouchable(button: ClickButton) {
        button.run {
            syncColorsWithColorScheme = true
            color.a = 1f
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