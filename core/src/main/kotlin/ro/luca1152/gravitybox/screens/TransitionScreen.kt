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

import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.OverlayStage
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.clear
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class TransitionScreen(
    private val nextScreen: Class<out KtxScreen>,
    private val fadeOutCurrentScreen: Boolean = true,
    private val game: MyGame = Injekt.get(),
    private val uiStage: UIStage = Injekt.get(),
    private val gameStage: GameStage = Injekt.get(),
    private val overlayStage: OverlayStage = Injekt.get(),
    private val engine: PooledEngine = Injekt.get()
) : KtxScreen {
    companion object {
        private const val FADE_DURATION = .75f
    }

    private var screenIsHidden = false
    private var currentScreen = game.shownScreen
    private val finishedFadingOut
        get() = uiStage.actors.size == 0 && gameStage.actors.size == 0 && overlayStage.actors.size == 0

    init {
        fadeOutEverything()
    }

    private fun fadeOutEverything() {
        if (fadeOutCurrentScreen) {
            uiStage.addAction(Actions.sequence(
                Actions.fadeOut(FADE_DURATION),
                Actions.run {
                    uiStage.clear()
                }
            ))
            gameStage.addAction(Actions.sequence(
                Actions.fadeOut(FADE_DURATION),
                Actions.run {
                    gameStage.clear()
                }
            ))
            overlayStage.addAction(Actions.sequence(
                Actions.fadeOut(FADE_DURATION),
                Actions.run {
                    overlayStage.clear()
                }
            ))
        } else {
            uiStage.clear()
            gameStage.clear()
            overlayStage.clear()

            // Fade everything out so the fade in will work
            uiStage.addAction(Actions.fadeOut(0f))
            gameStage.addAction(Actions.fadeOut(0f))
            overlayStage.addAction(Actions.fadeOut(0f))
        }
    }

    override fun render(delta: Float) {
        update(delta)
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        if (!screenIsHidden) {
            currentScreen.render(delta)
        }
    }

    private fun update(delta: Float) {
        uiStage.act(delta)
        gameStage.act(delta)
        overlayStage.act(delta)
        if (finishedFadingOut) {
            engine.clear()
            game.setScreen(nextScreen)
            fadeInEverything()
        }
    }

    private fun fadeInEverything() {
        uiStage.addAction(Actions.fadeIn(FADE_DURATION))
        gameStage.addAction(Actions.fadeIn(FADE_DURATION))
        overlayStage.addAction(Actions.fadeIn(FADE_DURATION))
    }

    override fun hide() {
        screenIsHidden = true
    }
}