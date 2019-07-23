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

package ro.luca1152.gravitybox.utils.ui.panes

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.components.game.network
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.systems.game.SkipLevelEvent
import ro.luca1152.gravitybox.utils.ads.AdsController
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class SkipLevelPane(context: Context) : Pane(context, 600f, 370f, context.inject()) {
    // Injected objects
    private val skin: Skin = context.inject()
    private val gameRules: GameRules = context.inject()
    private val adsController: AdsController? = context.injectNullable()
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val eventQueue: EventQueue = context.inject()
    private val playScreen: PlayScreen = context.inject()

    // Other panes
    private val skipLevelNoInternetPane = SkipLevelNoInternetPane(context)

    // This pane
    private val skipLevelButtonText = DistanceFieldLabel(context, "Skip level", skin, "regular", 36f, Color.WHITE)
    private val skipLevelTextButton = Button(skin, "long-button").apply {
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
                    eventQueue.add(SkipLevelEvent())
                    return
                }

                // Debug
                if (!gameRules.IS_MOBILE || adsController == null) {
                    gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN = gameRules.TIME_DELAY_BETWEEN_REWARDED_ADS
                    eventQueue.add(SkipLevelEvent())
                    return
                }

                if (!playScreen.networkEntity.network.isNetworkConnected) {
                    menuOverlayStage.addActor(skipLevelNoInternetPane)
                } else {
                    // Instantly hide the pop-up
                    this@SkipLevelPane.run {
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
            add(skipLevelTextButton).width(492f).padBottom(32f).row()
            add(noThanksButton).width(492f).row()
        }
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN <= 0f) {
            skipLevelButtonText.setText("Skip level")
            skipLevelTextButton.run {
                setColor(0 / 255f, 129 / 255f, 213 / 255f, 1f)
            }
        } else {
            skipLevelButtonText.setText("Wait ${secondsToTimeString(gameRules.TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN)}")
            skipLevelTextButton.run {
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