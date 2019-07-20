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

package ro.luca1152.gravitybox.utils.ui.playscreen

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.graphics.copy
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.UIViewport
import ro.luca1152.gravitybox.utils.kotlin.setWithoutAlpha
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.label.OutlineDistanceFieldLabel
import kotlin.math.min

class MenuOverlay(context: Context) {
    // Injected objects
    private val skin: Skin = context.inject()
    private val playScreen: PlayScreen = context.inject()
    private val manager: AssetManager = context.inject()
    private val uiStage: UIStage = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val gameRules: GameRules = context.inject()

    // Constants
    val bottomGrayStripHeight = 128f

    // Variables
    var shouldUpdateLevelLabel = false

    // Buttons
    private val leftButton = LeftButton(context)
    private val rightButton = RightButton(context)
    private val githubButton = GitHubButton(context)
    private val levelEditorButton = LevelEditorButton(context)
    private val heartButton = HeartButton(context)
    private val audioButton = AudioButton(context)

    // UI
    private val transparentImage = object : Image(manager.get(Assets.tileset).findRegion("pixel")) {
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

    // Labels
    private val levelLabel = object : OutlineDistanceFieldLabel(
        context,
        "#${when {
            gameRules.PLAY_SPECIFIC_LEVEL != -1 -> gameRules.PLAY_SPECIFIC_LEVEL
            gameRules.CAN_PLAY_ANY_LEVEL -> 1
            else -> min(gameRules.HIGHEST_FINISHED_LEVEL + 1, gameRules.LEVEL_COUNT)
        }}",
        skin, "semi-bold", 37f, Colors.gameColor
    ) {
        init {
            color.a = 1f
            addListener(object : ClickListener() {
                // Make the player not shoot if the label is clicked
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    return true
                }
            })
        }

        override fun act(delta: Float) {
            super.act(delta)
            updateLabel()
        }

        private fun updateLabel() {
            touchable = if (playScreen.levelEntity.level.isLevelFinished) Touchable.disabled else Touchable.enabled

            if (shouldUpdateLevelLabel) {
                setText("#${playScreen.levelEntity.level.levelId}")
                layout()
                shouldUpdateLevelLabel = false
            }
        }
    }
    private val levelMenuOverlayRankLabel = object : OutlineDistanceFieldLabel(
        context,
        "(Unranked)",
        skin, "semi-bold", 37f, Colors.gameColor
    ) {
        init {
            color.a = 1f
            addListener(object : ClickListener() {
                // Make the player not shoot if the label is clicked
                override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                    return true
                }
            })
        }

        override fun act(delta: Float) {
            super.act(delta)
            updateLabel()
        }

        private fun updateLabel() {
            touchable = if (playScreen.levelEntity.level.isLevelFinished) Touchable.disabled else Touchable.enabled

            val storedRank = gameRules.getGameLevelRank(playScreen.levelEntity.level.levelId)
            val storedHighscore = gameRules.getGameLevelHighscore(playScreen.levelEntity.level.levelId)
            if (storedRank != gameRules.DEFAULT_RANK_VALUE) {
                setText("(rank #$storedRank)")
            } else {
                if (storedHighscore == gameRules.SKIPPED_LEVEL_SCORE_VALUE) {
                    setText("(Skipped)")
                } else {
                    setText("(Unranked)")
                }
            }
            layout()
        }
    }

    // Tables
    val bottomGrayStrip = Table(skin).apply {
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
        @Suppress("UNUSED_VARIABLE")
        val middlePart = Table(skin).apply {
            add(audioButton)
        }
        val rightPart = Table(skin).apply {
            add(githubButton)
        }
        add(leftPart).padLeft(playScreen.padLeftRight).expand().left()
        add(rightPart).padRight(playScreen.padLeftRight).expand().right()
    }
    private val levelAndRankLabels = Table().apply {
        add(levelLabel).row()
        add(levelMenuOverlayRankLabel).row()
    }

    private val leftLevelRightTable = Table(skin).apply {
        add(leftButton).padRight(64f)
        add(levelAndRankLabels).padRight(64f)
        add(rightButton)
        addAction(Actions.fadeOut(0f))
    }
    private val topPart = Table(skin).apply {
        addActor(transparentImage)
        if (gameRules.ENABLE_LEVEL_EDITOR) {
            add(levelEditorButton).expand().top().left().padLeft(-levelEditorButton.prefWidth).padTop(playScreen.padTopBottom).row()
        }
        add(leftLevelRightTable).expand().bottom()
    }
    val rootTable = Table().apply {
        setFillParent(true)
        add(topPart).expand().fill().row()
        add(bottomGrayStrip).fillX().height(bottomGrayStripHeight).bottom().padBottom(-128f)
    }

    fun makeHeartButtonFull() {
        heartButton.run {
            styleName = "white-full-round-button"
            style = skin.get(styleName, Button.ButtonStyle::class.java)
        }
    }

    fun hideDefaultOverlay(): RunnableAction {
        return RunnableAction().apply {
            setRunnable {
                playScreen.run {
                    menuButton.touchable = Touchable.disabled
                    skipLevelButton.run {
                        addAction(Actions.moveBy(-270f, 0f, .2f, Interpolation.pow3In))
                    }
                    noAdsButton.run {
                        addAction(Actions.moveBy(-270f, 0f, .2f, Interpolation.pow3In))
                    }
                    leaderboardButton.run {
                        addAction(Actions.moveBy(270f, 0f, .2f, Interpolation.pow3In))
                    }
                    restartButton.run {
                        addAction(Actions.moveBy(270f, 0f, .2f, Interpolation.pow3In))
                    }
                    rankLabel.run {
                        addAction(
                            Actions.parallel(
                                Actions.moveTo(x, y - 100f - bottomGrayStripHeight, .2f, Interpolation.pow3In),
                                Actions.fadeOut(.2f, Interpolation.pow3In)
                            )
                        )
                    }
                }
            }
        }
    }

    fun showMenuOverlay(): RunnableAction {
        return RunnableAction().apply {
            setRunnable {
                levelEditorButton.run {
                    addAction(Actions.moveTo(x + playScreen.padLeftRight + prefWidth, y, .2f, Interpolation.pow3In))
                }
                bottomGrayStrip.run {
                    addAction(Actions.moveTo(0f, 0f, .2f, Interpolation.pow3In))
                }
                transparentImage.touchable = Touchable.enabled
                transparentImage.addAction(
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
        }
    }

    fun hideMenuOverlay() {
        // Hide menu overlay
        levelEditorButton.run {
            addAction(Actions.moveTo(x - playScreen.padLeftRight - prefWidth, y, .2f, Interpolation.pow3In))
        }
        bottomGrayStrip.run {
            addAction(
                Actions.sequence(
                    Actions.delay(.1f),
                    Actions.moveTo(0f, -bottomGrayStripHeight, .2f, Interpolation.pow3In)
                )
            )
        }
        transparentImage.touchable = Touchable.disabled
        transparentImage.addAction(
            Actions.sequence(
                Actions.delay(.1f),
                Actions.parallel(
                    Actions.moveTo(0f, 0f, .2f, Interpolation.pow3In),
                    Actions.color(Color.WHITE.copy(alpha = 0f), .3f)
                )
            )
        )

        // Show default overlay
        playScreen.run {
            skipLevelButton.run {
                addAction(
                    Actions.sequence(
                        Actions.delay(.1f),
                        Actions.moveBy(270f, 0f, .2f, Interpolation.pow3In)
                    )
                )
            }
            noAdsButton.run {
                addAction(
                    Actions.sequence(
                        Actions.delay(.1f),
                        Actions.moveBy(270f, 0f, .2f, Interpolation.pow3In)
                    )
                )
            }
            leaderboardButton.run {
                addAction(
                    Actions.sequence(
                        Actions.delay(.1f),
                        Actions.moveBy(-270f, 0f, .2f, Interpolation.pow3In)
                    )
                )

            }
            restartButton.run {
                addAction(
                    Actions.sequence(
                        Actions.delay(.1f),
                        Actions.moveBy(-270f, 0f, .2f, Interpolation.pow3In)
                    )
                )
            }
            menuButton.addAction(
                Actions.sequence(
                    Actions.delay(.1f),
                    Actions.parallel(
                        Actions.moveTo(menuButton.x, 25f, .2f, Interpolation.pow3In),
                        Actions.run {
                            if (!levelEntity.level.isLevelFinished) {
                                menuButton.addAction(Actions.fadeIn(.2f, Interpolation.pow3In))
                            }
                        }
                    ),
                    Actions.run {
                        if (!levelEntity.level.isLevelFinished) {
                            menuButton.touchable = Touchable.enabled
                        }
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
            rankLabel.run {
                addAction(
                    Actions.sequence(
                        Actions.delay(.1f),
                        Actions.parallel(
                            Actions.moveTo(x, y + 100f + bottomGrayStripHeight, .2f, Interpolation.pow3In),
                            Actions.run {
                                if (!levelEntity.level.isLevelFinished) {
                                    rankLabel.addAction(Actions.fadeIn(.2f, Interpolation.pow3In))
                                }
                            }
                        )
                    )
                )
            }
        }
    }
}