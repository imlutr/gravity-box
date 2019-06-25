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

import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ReturnValue
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult
import ktx.inject.Context
import ro.luca1152.gravitybox.GameRules

class ShotsLeaderboard(context: Context) {
    // Injected objects
    private val gameRules: GameRules = context.inject()
    private val dynamoDBClient: AmazonDynamoDBAsyncClient = context.inject()

    private fun incrementPlayerCountForShotsBy(level: Int, shots: Int, increment: Int) {
        val updateItemRequest = UpdateItemRequest()
            .withTableName(gameRules.SHOTS_LEADERBOARD_TABLE_NAME)
            .withKey(mapOf(Pair("Level ID", AttributeValue().withS("game-$level"))))
            .withUpdateExpression("SET Scores.#S = if_not_exists(Scores.#S, :zero) + :inc")
            .withExpressionAttributeNames(mapOf(Pair("#S", "$shots")))
            .withExpressionAttributeValues(
                mapOf(
                    Pair(":inc", AttributeValue().withN("$increment")),
                    Pair(":zero", AttributeValue().withN("0"))
                )
            )
            .withReturnValues(ReturnValue.UPDATED_NEW)

        dynamoDBClient.updateItemAsync(updateItemRequest, object : AsyncHandler<UpdateItemRequest, UpdateItemResult> {
            override fun onSuccess(request: UpdateItemRequest?, result: UpdateItemResult) {
                // Delete the score if the players that finished it in [shots] shots is now 0 (or, for some reason, less than 0)
                if (increment < 0) {
                    val stringPlayerCount = result.attributes["Scores"]!!.m["$shots"]?.n
                    if (stringPlayerCount != null) {
                        val intPlayerCount = Integer.parseInt(stringPlayerCount)
                        if (intPlayerCount <= 0) {
                            val deleteAttributeRequest = UpdateItemRequest()
                                .withTableName(gameRules.SHOTS_LEADERBOARD_TABLE_NAME)
                                .withKey(mapOf(Pair("Level ID", AttributeValue().withS("game-$level"))))
                                .withConditionExpression("Scores.#S <= :zero")
                                .withUpdateExpression("REMOVE Scores.#S")
                                .withExpressionAttributeNames(mapOf(Pair("#S", "$shots")))
                                .withExpressionAttributeValues(mapOf(Pair(":zero", AttributeValue().withN("0"))))
                            dynamoDBClient.updateItemAsync(deleteAttributeRequest)
                        }
                    }
                }
            }

            override fun onError(exception: Exception?) {}
        })
    }

    fun incrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, 1)
    }

    fun decrementPlayerCountForShots(level: Int, shots: Int) {
        incrementPlayerCountForShotsBy(level, shots, -1)
    }
}