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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class GitHubPane(context: Context, skin: Skin) : Pane(context, 600f, 440f, skin) {
    private val text = DistanceFieldLabel(
        context,
        """
            This game is fully open-source!

            Maybe star the GitHub repository
            if you like the project. <3
            """.trimIndent(), skin, "regular", 36f, skin.getColor("text-gold")
    )
    private val visitGithubButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(
            context,
            "Visit GitHub repository",
            skin, "regular", 36f, Color.WHITE
        )
        add(buttonText)
        color.set(0 / 255f, 129 / 255f, 213 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                Gdx.net.openURI("https://github.com/Luca1152/gravity-box")
                this@GitHubPane.hide()
            }
        })
    }
    private val maybeLaterButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(
            context,
            "Maybe later",
            skin, "regular", 36f, Color.WHITE
        )
        add(buttonText)
        color.set(99 / 255f, 116 / 255f, 132 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@GitHubPane.hide()
            }
        })
    }

    init {
        widget.run {
            add(text).padBottom(33f).expand().top().row()
            add(visitGithubButton).width(492f).padBottom(32f).row()
            add(maybeLaterButton).width(492f).row()
        }
    }

}