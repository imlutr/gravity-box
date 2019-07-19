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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Preferences
import ktx.inject.Context
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.kotlin.info
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class ClearPreferencesListener(context: Context) : InputAdapter() {
    // Injected objects
    private val preferences: Preferences = context.inject()

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.F5 && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            preferences.run {
                clear()
                flush()
            }
            info("Cleared all preferences.")
            return true
        }
        return false
    }

}

class BackKeyListener(context: Context) : InputAdapter() {
    // Injected objects
    private val menuOverlayStage: MenuOverlayStage = context.inject()
    private val playScreen: PlayScreen = context.inject()

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.BACK) {
            val popUp = menuOverlayStage.root.findActor<Pane>("Pane")
            if (popUp != null && !popUp.hasActions()) {
                popUp.backButtonRunnable.run()
            } else {
                playScreen.run {
                    when {
                        menuOverlay.bottomGrayStrip.y == 0f -> menuOverlay.hideMenuOverlay()
                        exitGameConfirmationPane.stage == null -> {
                            menuOverlayStage.addActor(exitGameConfirmationPane)
                            exitGameConfirmationPane.toFront()
                        }
                    }
                }
            }
            return true
        }
        return false
    }

}