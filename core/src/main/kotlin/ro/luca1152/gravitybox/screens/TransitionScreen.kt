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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class TransitionScreen(
    currentScreen: Class<KtxScreen>,
    private val nextScreen: Class<out KtxScreen>,
    private val game: MyGame = Injekt.get(),
    private val shapeRenderer: ShapeRenderer = Injekt.get(),
    private val uiStage: UIStage = Injekt.get()
) : KtxScreen {
    companion object {
        private const val FADE_DURATION = .5f
    }

    private var fadeOutCurrentScreen = true
    private var fadeInNextScreen = false
    private var screenDrawn = game.getScreen(currentScreen)
    private var screenDrawnAlpha = 1f

    override fun render(delta: Float) {
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        screenDrawn.render(delta)
        updateWhichScreenIsDrawn()
        drawRectangleAbove()
        updateDrawnScreenAlpha(delta)
        finishTransition()
    }

    private fun drawRectangleAbove() {
        Gdx.gl20.glEnable(GL20.GL_BLEND)
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.run {
            setColor(
                ColorScheme.currentLightColor.r,
                ColorScheme.currentLightColor.g,
                ColorScheme.currentLightColor.b,
                1f - screenDrawnAlpha
            )
            begin(ShapeRenderer.ShapeType.Filled)
            rect(0f, 0f, uiStage.viewport.worldWidth, uiStage.viewport.worldHeight)
            end()
        }
        Gdx.gl20.glDisable(GL20.GL_BLEND)
    }

    private fun updateWhichScreenIsDrawn() {
        if (screenDrawnAlpha <= 0f) {
            screenDrawnAlpha = 0f
            uiStage.clear()
            screenDrawn = game.getScreen(nextScreen).apply {
                show()
            }
            fadeOutCurrentScreen = false
            fadeInNextScreen = true
        }
    }

    private fun updateDrawnScreenAlpha(delta: Float) {
        if (fadeOutCurrentScreen) {
            screenDrawnAlpha -= (1f / FADE_DURATION) * delta
        } else {
            screenDrawnAlpha += (1f / FADE_DURATION) * delta
        }
    }

    private fun finishTransition() {
        if (fadeInNextScreen && screenDrawnAlpha >= 1f) {
            // game.setScreen() will call show() again, so this evens things out
            screenDrawn.hide()

            game.setScreen(nextScreen)
        }
    }

    override fun hide() {
        // Unregister this screen so it can be used again
        game.removeScreen<TransitionScreen>()
    }
}