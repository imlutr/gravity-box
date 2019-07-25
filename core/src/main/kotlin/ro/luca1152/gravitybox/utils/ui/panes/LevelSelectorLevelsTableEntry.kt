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

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules
import ro.luca1152.gravitybox.utils.kotlin.MenuOverlayStage
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel

class LevelSelectorLevelsTableEntry(
    context: Context,
    levelSelectorPane: LevelSelectorPane,
    var levelId: Int = -1
) : Table(context.inject()) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val menuOverlayStage: MenuOverlayStage = context.inject()

    // Constants
    private val rank
        get() = gameRules.getGameLevelRank(levelId)
    private val rankPercentage: String
        get() {
            val percentageAsNumber = gameRules.getGameLevelRankPercentage(levelId)
            val percentageAsString = "%.1f".format(percentageAsNumber)
            return "${if (percentageAsString == "0.0") "0.1" else percentageAsString}%"
        }
    private val isLevelLocked
        get() = levelId > gameRules.HIGHEST_FINISHED_LEVEL + 1
    private val isLevelSkipped
        get() = gameRules.isGameLevelSkipped(levelId)
    private val isLevelUnranked
        get() = gameRules.isGameLevelUnranked(levelId)
    private val isLevelSpecial
        get() = isLevelLocked || isLevelSkipped || isLevelUnranked
    private val entryColor
        get() = skin.getColor(if (isLevelLocked) "text-grayed-gold" else "text-gold")

    // Labels
    private val specialLabel = DistanceFieldLabel(context, "[string]", skin, "regular", 36f, skin.getColor("text-gold"))
    private val levelLabel = DistanceFieldLabel(context, "#[levelId]", skin, "regular", 36f, skin.getColor("text-gold"))
    private val rankLabel = DistanceFieldLabel(context, "[rank]", skin, "regular", 36f, skin.getColor("text-gold"))
    private val rankPercentageLabel = DistanceFieldLabel(context, "[percentage]%", skin, "regular", 36f, skin.getColor("text-gold"))

    init {
        updateEntryForLevelId(levelId)
        touchable = Touchable.enabled
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (isLevelLocked) {
                    return false
                }

                super.touchDown(event, x, y, pointer, button)

                specialLabel.isTouchedDown = true
                levelLabel.isTouchedDown = true
                rankLabel.isTouchedDown = true
                rankPercentageLabel.isTouchedDown = true
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                specialLabel.isTouchedDown = false
                levelLabel.isTouchedDown = false
                rankLabel.isTouchedDown = false
                rankPercentageLabel.isTouchedDown = false
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                levelSelectorPane.hide()
                menuOverlayStage.addActor(PlayLevelConfirmationPane(context, levelId))
            }
        })
    }

    fun updateEntryForLevelId(levelId: Int) {
        this.levelId = levelId

        // Check if anything has to be updated
        if (levelLabel.textEquals("#$levelId") && rankLabel.textEquals("$rank") && rankPercentageLabel.textEquals(rankPercentage) &&
            ((isLevelSkipped && specialLabel.textEquals("Skipped")) || (isLevelLocked && specialLabel.textEquals("Locked")) ||
                    ((isLevelUnranked && !isLevelSkipped) && specialLabel.textEquals("Unranked")))
        ) return

        // Remove everything
        clearChildren()

        // Update everything
        specialLabel.run {
            setText(if (isLevelSkipped) "Skipped" else if (isLevelLocked) "Locked" else if (isLevelUnranked) "Unranked" else "")
            color = entryColor
        }
        levelLabel.run {
            setText("#$levelId")
            color = entryColor
        }
        rankLabel.run {
            setText("$rank")
            color = entryColor
        }
        rankPercentageLabel.run {
            setText(rankPercentage)
            color = entryColor
        }

        // Add everything needed
        add(levelLabel).width(84f)
        if (isLevelSpecial) {
            add(specialLabel).expand().width(151f).padLeft(50f)
        } else {
            add(rankLabel).width(123f).padLeft(65f)
            add(rankPercentageLabel).width(103f).padLeft(72f)
        }
    }
}