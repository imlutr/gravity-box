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

package ro.luca1152.gravitybox.utils.leaderboards

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import ktx.inject.Context

class ShotsLeaderboard(context: Context) {
    // Injected objects
    private val dynamoDB: DynamoDB = context.inject()

    private val table = dynamoDB.getTable("GravityBox-ShotsLeaderboard")

    private fun incrementPlayerCountForShotsBy(level: Int, shots: Int, increment: Int) {
        val expressionAttributeNames = hashMapOf(
            Pair("#S", "$shots")
        )
        val expressionAttributeValues = hashMapOf(
            Pair(":inc", increment),
            Pair(":zero", 0)
        )
        table.updateItem(
            "Level ID",
            "game-$level",
            "SET Scores.#S = if_not_exists(Scores.#S, :zero) + :inc",
            expressionAttributeNames,
            expressionAttributeValues as Map<String, Any>
        )
    }

    fun incrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, 1)
    }

    fun decrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, -1)
    }
}