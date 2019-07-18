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
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class LeaderboardPane(
    private val context: Context,
    private val skin: Skin,
    private val currentLevelId: Int
) : Pane(context, 600f, 736f, skin) {
    // Injected objects
    private val shotsLeaderboard: GameShotsLeaderboard? = context.injectNullable()

    // Constants
    private val rowHeight = 5f

    private val rankLabel = DistanceFieldLabel(context, "Rank", skin, "regular", 36f, skin.getColor("text-gold"))
    private val shotsLabel = DistanceFieldLabel(context, "Shots", skin, "regular", 36f, skin.getColor("text-gold"))
    private val playersLabel = DistanceFieldLabel(context, "Players", skin, "regular", 36f, skin.getColor("text-gold"))
    private val topTextTable = Table().apply {
        add(rankLabel).expand().left()
        add(shotsLabel).expand()
        add(playersLabel).expand().right()
    }
    private val yellowHorizontalLine = Image(skin.getDrawable("yellow-horizontal-line"))
    private val closeButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(context, "Close", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@LeaderboardPane.hide()
            }
        })
    }

    init {
        updateWidget()
    }

    private fun updateWidget() {
        widget.run {
            if (context.injectNullable<GameShotsLeaderboard>() != null) {
                add(topTextTable).expand().padLeft(35f).padRight(35f).fillX().top().row()
                add(yellowHorizontalLine).width(492f).expand().top().row()
                add(createLeaderboardTable()).padLeft(35f).padRight(35f).grow().row()
                add(closeButton).width(492f).expand().bottom().row()
            }
        }
    }

    private fun createRanksColumn() = Table(skin).apply {
        for (i in 1..10) {
            val rankILabel = DistanceFieldLabel(
                context, "#$i${if (i == 10) "+" else ""}",
                skin, "regular", 36f, skin.getColor("text-gold")
            )
            add(rankILabel).expand().spaceBottom(rowHeight).row()
        }
    }

    private fun createShotsColumn() = Table(skin).apply {
        var shotI = 1
        var shotsAdded = 0
        while (shotsAdded < 10 && shotsAdded < shotsLeaderboard!!.levels["l$currentLevelId"]!!.shots.size) {
            if (shotsLeaderboard.levels["l$currentLevelId"]!!.shots.containsKey("s$shotI")) {
                shotsAdded++
                val shotILabel = DistanceFieldLabel(
                    context, "$shotI${if (shotsAdded == 10) "+" else ""}",
                    skin, "regular", 36f, skin.getColor("text-gold")
                )
                add(shotILabel).expand().spaceBottom(rowHeight).row()
            }
            shotI++
        }
    }

    private fun createPlayersColumn() = Table(skin).apply {
        var totalPlayers = 0L
        shotsLeaderboard!!.levels["l$currentLevelId"]!!.shots.forEach {
            totalPlayers += it.value
        }

        var shotI = 1
        var percentagesAdded = 0
        var playersAdded = 0L
        while (percentagesAdded < 10 && percentagesAdded < shotsLeaderboard.levels["l$currentLevelId"]!!.shots.size) {
            if (shotsLeaderboard.levels["l$currentLevelId"]!!.shots.containsKey("s$shotI")) {
                percentagesAdded++
                val playersForIShots = shotsLeaderboard.levels["l$currentLevelId"]!!.shots["s$shotI"]!!
                if (percentagesAdded < 10) {
                    playersAdded += playersForIShots
                }
                val percentage = (if (percentagesAdded == 10) totalPlayers - playersAdded else playersForIShots) * 100f / totalPlayers
                val percentageAsString = "%.1f".format(percentage)
                val percentageILabel = DistanceFieldLabel(
                    context, "${if (percentageAsString == "0.0") "0.1" else percentageAsString}%",
                    skin, "regular", 36f, skin.getColor("text-gold")
                )
                add(percentageILabel).expand().spaceBottom(rowHeight).row()
            }
            shotI++
        }
    }

    private fun createLeaderboardTable() = Table(skin).apply {
        add(createRanksColumn()).expand().left()
        add(createShotsColumn()).expand()
        add(createPlayersColumn()).expand().right()
    }
}