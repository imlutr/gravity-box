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
import ktx.actors.isShown
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.utils.kotlin.injectNullable
import ro.luca1152.gravitybox.utils.leaderboards.GameShotsLeaderboard
import ro.luca1152.gravitybox.utils.leaderboards.Level
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane
import kotlin.math.min

class LeaderboardPane(
    private val context: Context,
    private val currentLevelId: Int
) : Pane(context, 600f, 736f, context.inject()) {
    // Injected objects
    private val skin: Skin = context.inject()
    private val gameRules: GameRules = context.inject()

    // Constants
    private val rowHeight = 5f

    // Doesn't have internet connection
    private val noInternetConnectionLabel = DistanceFieldLabel(
        context,
        """
            The leaderboard couldn't be
            loaded...
            
            Please check your internet
            connection.""".trimIndent(),
        skin, "regular", 36f, skin.getColor("text-gold")
    )
    private val okayButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(context, "Okay :(", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@LeaderboardPane.hide()
            }
        })
    }

    // Has internet connection
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
        resetWidget()
        widget.run {
            if (context.injectNullable<GameShotsLeaderboard>() != null) {
                add(topTextTable).padLeft(35f).padRight(35f).fillX().padBottom(13f).row()
                add(yellowHorizontalLine).width(492f).padBottom(13f).row()
                add(createLeaderboardTable()).padLeft(35f).padRight(35f).fill().row()
                add(closeButton).width(492f).expand().bottom().row()
            } else {
                add(noInternetConnectionLabel).expand().top().row()
                add(okayButton).width(492f).expand().bottom().row()
            }
        }
    }

    private fun resetWidget() {
        noInternetConnectionLabel.remove()
        okayButton.remove()
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (context.injectNullable<GameShotsLeaderboard>() != null && !topTextTable.isShown()) {
            updateWidget()
        }
    }

    private fun createRanksColumn() = Table(skin).apply {
        val shotsLeaderboard = context.injectNullable<GameShotsLeaderboard>()
        for (i in 1..min(10, shotsLeaderboard!!.levels[Level.levelsKeys.getValue(currentLevelId)]!!.shots.size)) {
            val currentRank = gameRules.getGameLevelRank(currentLevelId)
            val textColor = if (currentRank == i || (i == 10 && currentRank >= i && currentRank != gameRules.DEFAULT_RANK_VALUE))
                "text-dark-gold" else "text-gold"
            val rankILabel = DistanceFieldLabel(
                context, "#$i${if (i == 10) "+" else ""}",
                skin, "regular", 36f, skin.getColor(textColor)
            )
            add(rankILabel).expand().spaceBottom(rowHeight).row()
        }
    }

    private fun createShotsColumn() = Table(skin).apply {
        var shotI = 1
        var shotsAdded = 0
        val shotsLeaderboard = context.injectNullable<GameShotsLeaderboard>()
        while (shotsAdded < 10 && shotsAdded < shotsLeaderboard!!.levels[Level.levelsKeys.getValue(currentLevelId)]!!.shots.size) {
            if (shotsLeaderboard.levels[Level.levelsKeys.getValue(currentLevelId)]!!.shots.containsKey(Level.shotsKeys(shotI))) {
                shotsAdded++
                val currentRank = gameRules.getGameLevelRank(currentLevelId)
                val textColor =
                    if (currentRank == shotsAdded || (shotsAdded == 10 && currentRank >= shotsAdded && currentRank != gameRules.DEFAULT_RANK_VALUE))
                        "text-dark-gold" else "text-gold"
                val shotILabel = DistanceFieldLabel(
                    context, "$shotI${if (shotsAdded == 10) "+" else ""}",
                    skin, "regular", 36f, skin.getColor(textColor)
                )
                add(shotILabel).expand().spaceBottom(rowHeight).row()
            }
            shotI++
        }
    }

    private fun createPlayersColumn() = Table(skin).apply {
        var totalPlayers = 0L
        val shotsLeaderboard = context.injectNullable<GameShotsLeaderboard>()
        shotsLeaderboard!!.levels[Level.levelsKeys.getValue(currentLevelId)]!!.shots.forEach {
            totalPlayers += it.value
        }

        var shotI = 1
        var percentagesAdded = 0
        var playersAdded = 0L
        while (percentagesAdded < 10 && percentagesAdded < shotsLeaderboard.levels[Level.levelsKeys.getValue(currentLevelId)]!!.shots.size) {
            if (shotsLeaderboard.levels[Level.levelsKeys.getValue(currentLevelId)]!!.shots.containsKey(Level.shotsKeys(shotI))) {
                percentagesAdded++
                val currentRank = gameRules.getGameLevelRank(currentLevelId)
                val textColor =
                    if (currentRank == percentagesAdded || (percentagesAdded == 10 && currentRank >= percentagesAdded && currentRank != gameRules.DEFAULT_RANK_VALUE))
                        "text-dark-gold" else "text-gold"
                val playersForIShots = shotsLeaderboard.levels[Level.levelsKeys.getValue(currentLevelId)]!!.shots[Level.shotsKeys(shotI)]!!
                if (percentagesAdded < 10) {
                    playersAdded += playersForIShots
                }
                val percentage = (if (percentagesAdded == 10) totalPlayers - playersAdded else playersForIShots) * 100f / totalPlayers
                val percentageAsString = "%.1f".format(percentage)
                val percentageILabel = DistanceFieldLabel(
                    context, "${if (percentageAsString == "0.0") "0.1" else percentageAsString}%",
                    skin, "regular", 36f, skin.getColor(textColor)
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