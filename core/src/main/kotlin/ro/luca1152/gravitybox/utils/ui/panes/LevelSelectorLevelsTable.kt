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

import com.badlogic.gdx.scenes.scene2d.ui.Table
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules

data class MutablePair<T, U>(var first: T, var second: U)

class LevelSelectorLevelsTable(context: Context, levelSelectorPane: LevelSelectorPane) : Table(context.inject()) {
    // Injected objects
    private val gameRules: GameRules = context.inject()

    // Sorting
    var sortingList = mutableListOf<MutablePair<Int, Float>>().apply {
        for (i in 1..gameRules.LEVEL_COUNT) {
            add(i - 1, MutablePair(i, 0f))
        }
    }

    init {
        for (i in 1..gameRules.LEVEL_COUNT) {
            add(LevelSelectorLevelsTableEntry(context, levelSelectorPane, i)).spaceBottom(5f).grow().row()
        }
    }

    fun sortByLevel(sortOrder: SortOrder) {
        val lockedValue = if (sortOrder == SortOrder.DESCENDING) Float.NEGATIVE_INFINITY else Float.POSITIVE_INFINITY
        for (i in 0 until gameRules.LEVEL_COUNT) {
            sortingList[i].first = i + 1
            when {
                i > gameRules.HIGHEST_FINISHED_LEVEL -> sortingList[i].second = lockedValue
                else -> sortingList[i].second = i + 1f
            }
        }
        when (sortOrder) {
            SortOrder.ASCENDING -> sortingList.sortBy { it.second }
            SortOrder.DESCENDING -> sortingList.sortByDescending { it.second }
            else -> error("Invalid sort order.")
        }
        var i = 0
        (this as Table).children.forEach {
            (it as LevelSelectorLevelsTableEntry).updateEntryForLevelId(sortingList[i].first)
            i++
        }
    }

    fun sortByRank(sortOrder: SortOrder) {
        val skippedValue = if (sortOrder == SortOrder.ASCENDING) Float.POSITIVE_INFINITY - 2f else Float.POSITIVE_INFINITY
        val unrankedValue = if (sortOrder == SortOrder.ASCENDING) Float.POSITIVE_INFINITY - 1f else -1f
        val lockedValue = if (sortOrder == SortOrder.ASCENDING) Float.POSITIVE_INFINITY else -2f
        for (i in 0 until gameRules.LEVEL_COUNT) {
            sortingList[i].first = i + 1
            when {
                i > gameRules.HIGHEST_FINISHED_LEVEL -> sortingList[i].second = lockedValue
                gameRules.isGameLevelSkipped(i + 1) -> sortingList[i].second = skippedValue
                gameRules.isGameLevelUnranked(i + 1) -> sortingList[i].second = unrankedValue
                else -> sortingList[i].second = gameRules.getGameLevelRank(i + 1).toFloat()
            }
        }
        when (sortOrder) {
            SortOrder.ASCENDING -> sortingList.sortBy { it.second }
            SortOrder.DESCENDING -> sortingList.sortByDescending { it.second }
            else -> error("Invalid sort order.")
        }
        var i = 0
        (this as Table).children.forEach {
            (it as LevelSelectorLevelsTableEntry).updateEntryForLevelId(sortingList[i].first)
            i++
        }
    }

    fun sortByRankPercentage(sortOrder: SortOrder) {
        val skippedValue = if (sortOrder == SortOrder.ASCENDING) Float.POSITIVE_INFINITY - 2f else Float.POSITIVE_INFINITY
        val unrankedValue = if (sortOrder == SortOrder.ASCENDING) Float.POSITIVE_INFINITY - 1f else -1f
        val lockedValue = if (sortOrder == SortOrder.ASCENDING) Float.POSITIVE_INFINITY else -2f
        for (i in 0 until gameRules.LEVEL_COUNT) {
            sortingList[i].first = i + 1
            when {
                i > gameRules.HIGHEST_FINISHED_LEVEL -> sortingList[i].second = lockedValue
                gameRules.isGameLevelSkipped(i + 1) -> sortingList[i].second = skippedValue
                gameRules.isGameLevelUnranked(i + 1) -> sortingList[i].second = unrankedValue
                else -> sortingList[i].second = gameRules.getGameLevelRankPercentage(i + 1)
            }
        }
        when (sortOrder) {
            SortOrder.ASCENDING -> sortingList.sortBy { it.second }
            SortOrder.DESCENDING -> sortingList.sortByDescending { it.second }
            else -> error("Invalid sort order.")
        }
        var i = 0
        (this as Table).children.forEach {
            (it as LevelSelectorLevelsTableEntry).updateEntryForLevelId(sortingList[i].first)
            i++
        }
    }

    fun update() {
        (this as Table).children.forEach {
            (it as LevelSelectorLevelsTableEntry).updateEntryForLevelId(it.levelId)
        }
    }
}

enum class SortOrder {
    ASCENDING,
    DESCENDING,
    UNSORTED
}