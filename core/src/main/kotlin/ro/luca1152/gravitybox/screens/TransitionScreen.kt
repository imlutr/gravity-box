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
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import ktx.app.KtxScreen
import ktx.inject.Context
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors

/** Transitions to the [nextScreen] with a fade animation.. */
class TransitionScreen(
    context: Context,
    private val nextScreen: Class<out KtxScreen>,
    private val fadeOutCurrentScreen: Boolean = true,
    private val clearScreenWithBlack: Boolean = false
) : KtxScreen {
    // Injected objects
    private val game: MyGame = context.inject()
    private val uiStage: UIStage = context.inject()
    private val gameStage: GameStage = context.inject()
    private val overlayStage: OverlayStage = context.inject()
    private val gameViewport: GameViewport = context.inject()
    private val uiViewport: UIViewport = context.inject()
    private val overlayViewport: OverlayViewport = context.inject()
    private val engine: PooledEngine = context.inject()

    private val previousScreen = game.shownScreen
    private var currentScreen = game.shownScreen
    private val fadeDuration = .5f
    private var transitionScreenIsHidden = false
    private val finishedFadingOut
        get() = uiStage.root.actions.size == 0

    init {
        if (currentScreen !is TransitionScreen) {
            Gdx.input.inputProcessor = null
            fadeOutEverything()
        }
    }

    private fun fadeOutEverything() {
        if (fadeOutCurrentScreen) {
            uiStage.addAction(Actions.fadeOut(fadeDuration))
            gameStage.addAction(Actions.fadeOut(fadeDuration))
            overlayStage.addAction(Actions.fadeOut(fadeDuration))
        }
    }

    override fun render(delta: Float) {
        update()
        clearScreen(if (clearScreenWithBlack) Color.BLACK else Colors.bgColor)
        if (!transitionScreenIsHidden && currentScreen !is TransitionScreen) {
            currentScreen.render(delta)
        } else if (currentScreen is TransitionScreen) {
            previousScreen.render(delta)
        }
    }

    private fun update() {
        Colors.lerpTowardsDefaultColors(.1f)
        if (finishedFadingOut || !fadeOutCurrentScreen) {
            cleanUp()
            game.setScreen(nextScreen)
            fadeInEverything()
        }
    }

    private fun cleanUp() {
        uiStage.clear()
        gameStage.clear()
        overlayStage.clear()
        engine.clear()
        Colors.resetAllColors()
    }

    private fun fadeInEverything() {
        uiStage.addAction(Actions.fadeOut(0f))
        gameStage.addAction(Actions.fadeOut(0f))
        overlayStage.addAction(Actions.fadeOut(0f))
        uiStage.addAction(Actions.fadeIn(fadeDuration))
        gameStage.addAction(Actions.fadeIn(fadeDuration))
        overlayStage.addAction(Actions.fadeIn(fadeDuration))
    }

    override fun resize(width: Int, height: Int) {
        gameViewport.update(width, height, false)
        uiViewport.update(width, height, false)
        overlayViewport.update(width, height, false)
    }

    override fun hide() {
        transitionScreenIsHidden = true
    }
}