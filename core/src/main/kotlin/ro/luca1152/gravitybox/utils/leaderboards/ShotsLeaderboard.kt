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
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec
import com.amazonaws.services.dynamodbv2.document.utils.NameMap
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.ReturnValue
import ktx.inject.Context

class ShotsLeaderboard(context: Context) {
    // Injected objects
    private val dynamoDB: DynamoDB = context.inject()

    private val table = dynamoDB.getTable("GravityBox-ShotsLeaderboard")

    private fun incrementPlayerCountForShotsBy(level: Int, shots: Int, increment: Int) {
        val updateItemSpec = UpdateItemSpec()
            .withPrimaryKey("Level ID", "game-$level")
            .withUpdateExpression("SET Scores.#S = if_not_exists(Scores.#S, :zero) + :inc")
            .withNameMap(
                NameMap()
                    .with("#S", "$shots")
            )
            .withValueMap(
                ValueMap()
                    .withInt(":inc", increment)
                    .withInt(":zero", 0)
            )
            .withReturnValues(ReturnValue.UPDATED_NEW)
        val outcome = table.updateItem(updateItemSpec)

        // Delete the score if the players that finished it in [shots] shots is now 0 (or, for some reason, less than 0)
        if (increment < 0) {
            val stringPlayerCount = outcome.updateItemResult.attributes["Scores"]!!.m["$shots"]?.n
            if (stringPlayerCount != null) {
                val intPlayerCount = Integer.parseInt(stringPlayerCount)
                if (intPlayerCount <= 0) {
                    val deleteAttributeSpec = UpdateItemSpec()
                        .withPrimaryKey("Level ID", "game-$level")
                        .withConditionExpression("Scores.#S <= :zero")
                        .withUpdateExpression("REMOVE Scores.#S")
                        .withNameMap(
                            NameMap()
                                .with("#S", "$shots")
                        )
                        .withValueMap(
                            ValueMap()
                                .withInt(":zero", 0)
                        )
                    table.updateItem(deleteAttributeSpec)
                }
            }
        }
    }

    fun incrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, 1)
    }

    fun decrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, -1)
    }
}