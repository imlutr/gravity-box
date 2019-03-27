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
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.app.KtxScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Transitions to the [nextScreen] with a fade animation.. */
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
        private const val FADE_DURATION = .5f
    }

    private var transitionScreenIsHidden = false
    private var currentScreen = game.shownScreen
    private val finishedFadingOut
        get() = uiStage.root.actions.size == 0

    init {
        if (currentScreen !is TransitionScreen) {
            Gdx.input.inputProcessor = null
            fadeOutEverything()
            game.transitionOldScreen = currentScreen
        }
    }

    private fun fadeOutEverything() {
        if (fadeOutCurrentScreen) {
            uiStage.addAction(Actions.fadeOut(FADE_DURATION))
            gameStage.addAction(Actions.fadeOut(FADE_DURATION))
            overlayStage.addAction(Actions.fadeOut(FADE_DURATION))
        }
    }

    override fun render(delta: Float) {
        update()
        clearScreen(Colors.bgColor)
        if (!transitionScreenIsHidden && currentScreen !is TransitionScreen) {
            currentScreen.render(delta)
        } else if (currentScreen is TransitionScreen) {
            game.transitionOldScreen!!.render(delta)
        }
    }

    private fun update() {
        Colors.lerpTowardsDefaultColors(.1f)
        if (finishedFadingOut || !fadeOutCurrentScreen) {
            Colors.resetAllColors()
            uiStage.clear()
            gameStage.clear()
            overlayStage.clear()
            engine.clear()
            game.setScreen(nextScreen)
            fadeInEverything()
        }
    }

    private fun fadeInEverything() {
        uiStage.addAction(Actions.fadeOut(0f))
        gameStage.addAction(Actions.fadeOut(0f))
        overlayStage.addAction(Actions.fadeOut(0f))
        uiStage.addAction(Actions.fadeIn(FADE_DURATION))
        gameStage.addAction(Actions.fadeIn(FADE_DURATION))
        overlayStage.addAction(Actions.fadeIn(FADE_DURATION))
    }

    override fun hide() {
        transitionScreenIsHidden = true
    }
}