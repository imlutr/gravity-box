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

package ro.luca1152.gravitybox.systems.game

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import ktx.inject.Context
import ro.luca1152.gravitybox.events.EventSystem
import ro.luca1152.gravitybox.events.FadeInEvent
import ro.luca1152.gravitybox.events.FadeOutEvent
import ro.luca1152.gravitybox.events.FadeOutFadeInEvent
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.setWithoutAlpha
import ro.luca1152.gravitybox.utils.ui.Colors

/** Handles [FadeOutEvent]s, [FadeInEvent]s and [FadeOutFadeInEvent]s. */
class FadeOutFadeInSystem(context: Context) :
    EventSystem<FadeOutFadeInEvent>(context.inject(), FadeOutFadeInEvent::class) {
    // Injected objects
    private val manager: AssetManager = context.inject()
    private val gameStage: GameStage = context.inject()

    private val whiteImage = object : Image(manager.get(Assets.tileset).findRegion("pixel")) {
        init {
            color.set(Colors.bgColor)
            color.a = 0f
            setSize(gameStage.viewport.worldWidth * 10f, gameStage.viewport.worldHeight * 10f)
        }

        override fun act(delta: Float) {
            super.act(delta)
            centerImage()
            color.setWithoutAlpha(Colors.bgColor)
        }

        /** The white image does is in the [GameStage], not [UIStage], thus is affected by the camera's position.*/
        private fun centerImage() {
            val cameraPosition = gameStage.camera.position
            x = cameraPosition.x - width / 2f
            y = cameraPosition.y - height / 2f
            toFront()
        }
    }

    override fun processEvent(event: FadeOutFadeInEvent, deltaTime: Float) {
        addEffect(event)
    }

    private fun addEffect(event: FadeOutFadeInEvent) {
        gameStage.addActor(whiteImage)
        whiteImage.run {
            if (event.fadeInDuration == FadeOutFadeInEvent.CLEAR_ACTIONS || event.fadeOutDuration == FadeOutFadeInEvent.CLEAR_ACTIONS) {
                clearActions()
            } else {
                if (event.fadeOutDuration != 0f) {
                    // Fading in the image = fade out
                    addAction(Actions.fadeIn(event.fadeOutDuration, event.fadeOutInterpolation))
                }
                if (event.fadeInDuration != 0f) {
                    // Fading out the image = fade in
                    addAction(Actions.after(Actions.fadeOut(event.fadeInDuration, event.fadeInInterpolation)))
                    addAction(Actions.after(Actions.removeActor()))
                }
            }
        }
    }
}