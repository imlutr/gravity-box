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
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import ktx.graphics.copy
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.kotlin.setWithoutAlpha
import ro.luca1152.gravitybox.utils.ui.label.DistanceFieldLabel
import ro.luca1152.gravitybox.utils.ui.popup.Pane

class LevelSelectorPane(private val context: Context) : Pane(context, 600f, 736f, context.inject()) {
    // Injected objects
    private val skin: Skin = context.inject()

    // UI
    private val levelLabel = DistanceFieldLabel(context, "Level", skin, "regular", 36f, skin.getColor("text-gold"))
    private val levelSortImage = object : Image(skin.getDrawable("ascending-sort-icon")) {
        init {
            color = skin.getColor("text-gold")
        }

        override fun act(delta: Float) {
            super.act(delta)
            color.setWithoutAlpha(levelLabel.color)
            when (sortLevelOrder) {
                SortOrder.ASCENDING -> {
                    setDrawable(skin, "ascending-sort-icon")
                    color.a = 1f
                }
                SortOrder.DESCENDING -> {
                    setDrawable(skin, "descending-sort-icon")
                    color.a = 1f
                }
                else -> color.a = 0f
            }
        }
    }
    private val rankLabel = DistanceFieldLabel(context, "Rank", skin, "regular", 36f, skin.getColor("text-gold"))
    private val rankSortImage = object : Image(skin.getDrawable("descending-sort-icon")) {
        init {
            color = skin.getColor("text-gold").copy(alpha = 0f)
        }

        override fun act(delta: Float) {
            super.act(delta)
            color.setWithoutAlpha(rankLabel.color)
            when (sortRankOrder) {
                SortOrder.ASCENDING -> {
                    setDrawable(skin, "ascending-sort-icon")
                    color.a = 1f
                }
                SortOrder.DESCENDING -> {
                    setDrawable(skin, "descending-sort-icon")
                    color.a = 1f
                }
                else -> color.a = 0f
            }
        }
    }
    private val percentLabel = DistanceFieldLabel(context, "Percent", skin, "regular", 36f, skin.getColor("text-gold"))
    private val percentSortImage = object : Image(skin.getDrawable("descending-sort-icon")) {
        init {
            color = skin.getColor("text-gold").copy(alpha = 0f)
        }

        override fun act(delta: Float) {
            super.act(delta)
            color.setWithoutAlpha(percentLabel.color)
            when (sortRankPercentageOrder) {
                SortOrder.ASCENDING -> {
                    setDrawable(skin, "ascending-sort-icon")
                    color.a = 1f
                }
                SortOrder.DESCENDING -> {
                    setDrawable(skin, "descending-sort-icon")
                    color.a = 1f
                }
                else -> color.a = 0f
            }
        }
    }
    private val yellowHorizontalLine = Image(skin.getDrawable("yellow-horizontal-line"))

    // Tables
    private val levelLabelAndSortImageTable = Table().apply {
        add(levelLabel).padRight(8f)
        add(levelSortImage)
        touchable = Touchable.enabled
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                levelLabel.isTouchedDown = true
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                levelLabel.isTouchedDown = false
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                sortBy(byLevelId = true)
            }
        })
    }
    private val rankLabelAndSortImageTable = Table().apply {
        add(rankLabel).padRight(8f)
        add(rankSortImage)
        touchable = Touchable.enabled
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                rankLabel.isTouchedDown = true
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                rankLabel.isTouchedDown = false
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                sortBy(byRank = true)
            }
        })
    }
    private val percentLabelAndSortImageTable = Table().apply {
        add(percentLabel).padRight(8f)
        add(percentSortImage)
        touchable = Touchable.enabled
        addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                super.touchDown(event, x, y, pointer, button)
                percentLabel.isTouchedDown = true
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                super.touchUp(event, x, y, pointer, button)
                percentLabel.isTouchedDown = false
            }

            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                sortBy(byRankPercentage = true)
            }
        })
    }
    private val topTextTable = Table().apply {
        add(levelLabelAndSortImageTable).expand().left()
        add(rankLabelAndSortImageTable).expand().padRight((-8f - rankSortImage.width) / 2f)
        add(percentLabelAndSortImageTable).expand().right().padRight(-8f - percentSortImage.width)
    }

    // Sort
    var sortLevelOrder = SortOrder.ASCENDING
    var sortRankOrder = SortOrder.UNSORTED
    var sortRankPercentageOrder = SortOrder.UNSORTED

    // Scroll pane
    private val levelsTable = LevelSelectorLevelsTable(context, this)
    private val scrollPane = ScrollPane(levelsTable)

    // Buttons
    private val closeButton = Button(skin, "long-button").apply {
        val buttonText = DistanceFieldLabel(context, "Close", skin, "regular", 36f, Color.WHITE)
        add(buttonText)
        color.set(140 / 255f, 182 / 255f, 198 / 255f, 1f)
        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                this@LevelSelectorPane.hide()
            }
        })
    }

    init {
        widget.run {
            add(topTextTable).padLeft(35f).padRight(35f).fillX().padBottom(13f).row()
            add(yellowHorizontalLine).width(492f).height(3f).padBottom(13f).row()
            add(scrollPane).padLeft(35f).padRight(35f).grow().row()
            add(closeButton).width(492f).expand().bottom().row()
        }
    }

    private fun sortBy(byLevelId: Boolean = false, byRank: Boolean = false, byRankPercentage: Boolean = false) {
        when {
            byLevelId -> sortByOrdered(levelIdSortOrder = if (sortLevelOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING)
            byRank -> sortByOrdered(rankSortOrder = if (sortRankOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING)
            byRankPercentage -> sortByOrdered(rankPercentageSortOrder = if (sortRankPercentageOrder == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING)
        }
        scrollPane.run {
            scrollY = 0f
            velocityY = 0f
        }
    }

    private fun sortByOrdered(
        levelIdSortOrder: SortOrder = SortOrder.UNSORTED,
        rankSortOrder: SortOrder = SortOrder.UNSORTED,
        rankPercentageSortOrder: SortOrder = SortOrder.UNSORTED
    ) {
        sortLevelOrder = levelIdSortOrder
        sortRankOrder = rankSortOrder
        sortRankPercentageOrder = rankPercentageSortOrder
        when {
            levelIdSortOrder != SortOrder.UNSORTED -> levelsTable.sortByLevel(sortLevelOrder)
            rankSortOrder != SortOrder.UNSORTED -> levelsTable.sortByRank(sortRankOrder)
            rankPercentageSortOrder != SortOrder.UNSORTED -> levelsTable.sortByRankPercentage(sortRankPercentageOrder)
        }
    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)
        if (stage != null) {
            levelsTable.update()
            sortByOrdered(sortLevelOrder, sortRankOrder, sortRankPercentageOrder)
        }
    }
}