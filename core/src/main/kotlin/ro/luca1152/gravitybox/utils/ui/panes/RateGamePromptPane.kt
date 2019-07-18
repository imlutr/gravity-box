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

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class RateGamePromptPane(context: Context, skin: Skin, makeHeartButtonFull: () -> Unit) : Pane(context, 600f, 570f, skin) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    private val text = DistanceFieldLabel(
        context,
        """
            Would you like to rate the game
            or give feedback?

            (I actually read every review)
        """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
    )
    private val rateButton = Button(skin, "long-button").apply {
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
                this@RateGamePromptPane.hide()
            }
        })
    }
    private val maybeLaterButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(context, "Maybe later", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                gameRules.MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN =
                    gameRules.PLAY_TIME + gameRules.TIME_DELAY_BETWEEN_PROMPTING_USER_TO_RATE_THE_GAME_AGAIN
                this@RateGamePromptPane.hide()
            }
        })
    }
    private val neverButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(context, "Never :(", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                gameRules.NEVER_PROMPT_USER_TO_RATE_THE_GAME = true
                this@RateGamePromptPane.hide()
            }
        })
    }

    init {
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
}