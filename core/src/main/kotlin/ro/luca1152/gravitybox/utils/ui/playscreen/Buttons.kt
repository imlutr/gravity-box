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

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.level
import ro.luca1152.gravitybox.components.game.map
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.systems.game.FadeInEvent
import ro.luca1152.gravitybox.systems.game.FadeOutEvent
import ro.luca1152.gravitybox.systems.game.UpdateRoundedPlatformsEvent
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import ro.luca1152.gravitybox.utils.ui.panes.LeaderboardPane
import ro.luca1152.gravitybox.utils.ui.panes.NoAdsPane

class RestartButton(context: Context) : ClickButton(context.inject(), "color-round-button-padded") {
    // Injected objects
    private val playScreen: PlayScreen = context.inject()
    private val gameRules: GameRules = context.inject()

    init {
        addIcon("restart-icon")
        addClickRunnable(Runnable {
            if (!playScreen.levelEntity.level.isRestarting) {
                playScreen.levelEntity.level.restartLevel = true
                gameRules.RESTART_COUNT++
            }
        })
    }
}

class SkipLevelButton(context: Context) : ClickButton(context.inject(), "color-round-button-padded") {
    // Injected objects
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val playScreen: PlayScreen = context.inject()

    init {
        addIcon("skip-level-icon")
        iconCell!!.padLeft(6f) // The icon doesn't look centered
        addClickRunnable(Runnable {
            menuOverlayStage.addActor(playScreen.skipLevelPane)
        })
    }
}

class NoAdsButton(context: Context) : ClickButton(context.inject(), "empty-round-button-padded") {
    // Injected objects
    private val menuOverlayStage: MenuOverlayStage = context.inject()

    init {
        addIcon("no-ads-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(NoAdsPane(context))
            }
        })
    }
}

class LeaderboardButton(context: Context) : ClickButton(context.inject(), "empty-round-button-padded") {
    // Injected objects
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val playScreen: PlayScreen = context.inject()

    init {
        addIcon("leaderboard-icon")
        addClickRunnable(Runnable {
            menuOverlayStage.addActor(LeaderboardPane(context, playScreen.levelEntity.level.levelId))
        })
    }
}

class GitHubButton(context: Context) : ClickButton(context.inject(), "empty-round-button") {
    // Injected objects
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val playScreen: PlayScreen = context.inject()

    init {
        addIcon("github-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(playScreen.gitHubPane)
            }
        })
    }
}

class LevelEditorButton(context: Context) : ClickButton(context.inject(), "gray-full-round-button") {
    // Injected objects
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val playScreen: PlayScreen = context.inject()

    init {
        addIcon("level-editor-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(playScreen.levelEditorPane)
            }
        })
    }
}

class HeartButton(context: Context) : ClickButton(
    context.inject(),
    if (context.inject<GameRules>().DID_RATE_THE_GAME) "white-full-round-button" else "empty-round-button"
) {
    // Injected objects
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val playScreen: PlayScreen = context.inject()

    init {
        addIcon("heart-icon")
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                menuOverlayStage.addActor(playScreen.heartPane)
            }
        })
    }
}

class AudioButton(context: Context) : ClickButton(context.inject(), "white-full-round-button") {
    private val order = arrayListOf("sounds-and-music-icon", "music-icon", "sounds-icon", "no-sounds-icon")
    private var current = order.first()

    init {
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

    private fun getNextIcon() = if (order.indexOf(current) == order.size - 1) order.first() else order[order.indexOf(current) + 1]
}

class MenuButton(context: Context) : ClickButton(context.inject(), "menu-button") {
    // Injected objects
    private val playScreen: PlayScreen = context.inject()
    private val menuOverlay = playScreen.menuOverlay

    init {
        addClickRunnable(Runnable {
            addAction(
                Actions.sequence(
                    menuOverlay.hideDefaultOverlay(),
                    Actions.delay(.1f),
                    Actions.parallel(
                        Actions.moveTo(x, menuOverlay.bottomGrayStripHeight, .2f, Interpolation.pow3In),
                        Actions.fadeOut(.2f, Interpolation.pow3In),
                        menuOverlay.showMenuOverlay()
                    )
                )
            )
        })
    }
}

@Suppress("ConstantConditionIf")
class LeftButton(context: Context) : ClickButton(context.inject(), "left-button") {
    // Injected objects
    private val gameStage: GameStage = context.inject()
    private val eventQueue: EventQueue = context.inject()
    private val playScreen: PlayScreen = context.inject()
    private val gameRules: GameRules = context.inject()

    init {
        touchable = Touchable.disabled
        addClickRunnable(Runnable {
            val levelEntity = playScreen.levelEntity
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
                            playScreen.menuOverlay.shouldUpdateLevelLabel = true
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

    override fun act(delta: Float) {
        super.act(delta)
        updateButton()
    }

    private fun updateButton() {
        styleName = if (playScreen.levelEntity.level.levelId == 1) "double-left-button" else "left-button"
        style = skin.get(styleName, Button.ButtonStyle::class.java)
    }
}

class RightButton(context: Context) : ClickButton(context.inject(), "right-button") {
    // Injected objects
    private val gameStage: GameStage = context.inject()
    private val eventQueue: EventQueue = context.inject()
    private val playScreen: PlayScreen = context.inject()
    private val gameRules: GameRules = context.inject()

    init {
        touchable = Touchable.disabled
        addClickRunnable(Runnable {
            val levelEntity = playScreen.levelEntity
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
                            playScreen.menuOverlay.shouldUpdateLevelLabel = true
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

    override fun act(delta: Float) {
        super.act(delta)
        updateButton()
    }

    private fun updateButton() {
        styleName =
            if ((gameRules.CAN_PLAY_ANY_LEVEL && playScreen.levelEntity.level.levelId == gameRules.LEVEL_COUNT) ||
                (!gameRules.CAN_PLAY_ANY_LEVEL && playScreen.levelEntity.level.levelId == gameRules.HIGHEST_FINISHED_LEVEL + 1)
            ) "double-right-button"
            else "right-button"
        style = skin.get(styleName, Button.ButtonStyle::class.java)
    }
}