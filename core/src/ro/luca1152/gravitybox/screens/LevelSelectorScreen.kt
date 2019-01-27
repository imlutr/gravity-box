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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ro.luca1152.gravitybox.MyGame
import ro.luca1152.gravitybox.utils.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class LevelSelectorScreen(batch: Batch = Injekt.get(),
                          private val manager: AssetManager = Injekt.get()) : KtxScreen {
    companion object {
        var chosenlevel = 0
    }

    private val uiStage = Stage(ExtendViewport(720f, 1280f), batch)

    override fun show() {
        val skin = manager.get<Skin>("skins/uiskin.json")

        val table = Table()
        table.width = 600f
        table.setPosition(60f, 1280f)
        table.center().top()
        uiStage.addActor(table)

        table.padTop(60f)

        for (level in 1..MyGame.LEVELS_NUMBER) {
            val button = TextButton(level.toString(), skin, "level-button")
            button.color = ColorScheme.darkerDarkColor
            button.label.color = ColorScheme.darkerDarkColor
            button.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    chosenlevel = level
                    Injekt.get<MyGame>().setScreen<PlayScreen>()
                }
            })
            when {
                level % 4 != 0 -> table.add(button).width(120f).height(120f).padRight(20f)
                else -> table.add(button).width(120f).height(120f).row()
            }
        }

        Gdx.input.inputProcessor = uiStage
    }

    override fun render(delta: Float) {
        clearScreen(ColorScheme.currentLightColor.r, ColorScheme.currentLightColor.g, ColorScheme.currentLightColor.b)
        uiStage.draw()
    }
}