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

@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate")

package ro.luca1152.gravitybox

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import ktx.inject.Context

class GameRules(context: Context) {
    private val preferences: Preferences = context.inject()

    // Stats
    var HIGHEST_FINISHED_LEVEL
        get() = preferences.getInteger("highestFinishedLevel", 0)
        set(value) {
            preferences.run {
                putInteger("highestFinishedLevel", value)
                flush()
            }
        }
    /** The time (in seconds) a user spent playing the game. */
    var PLAY_TIME
        get() = preferences.getFloat("playTime", 0f)
        set(value) {
            preferences.run {
                putFloat("playTime", value)
                flush()
            }
        }

    // Debug
    val CAN_LOAD_ANY_LEVEL = false
    val LOAD_SPECIFIC_LEVEL = -1

    // Rules
    val LEVEL_COUNT = 270
    val IS_MOBILE = Gdx.app.type == Application.ApplicationType.Android || Gdx.app.type == Application.ApplicationType.iOS
    val ENABLE_LEVEL_EDITOR = !IS_MOBILE

    // Rate-related
    val MIN_FINISHED_LEVELS_TO_SHOW_RATE_PROMPT = 13
    var DID_RATE_THE_GAME
        get() = preferences.getBoolean("didRateGame", false)
        set(value) {
            preferences.run {
                putBoolean("didRateGame", value)
                flush()
            }
        }
    /** True if the player pressed the `Never` button when asked to rate the game. */
    var NEVER_PROMPT_USER_TO_RATE_THE_GAME
        get() = preferences.getBoolean("neverPromptUserToRate", false)
        set(value) {
            preferences.run {
                putBoolean("neverPromptUserToRate", value)
                flush()
            }
        }
    val DELAY_BETWEEN_PROMPTING_USER_TO_RATE_THE_GAME_AGAIN = 7.5f * 60 // 7.5 minutes
    /** The player will be asked to rate the game again after the PLAY_TIME exceeds this value if he chose to rate the game `Later`.*/
    var MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN
        get() = preferences.getFloat("minPlayTimeToPromptUserToRateTheGameAgain", 0f)
        set(value) {
            preferences.run {
                putFloat("minPlayTimeToPromptUserToRateTheGameAgain", value)
                flush()
            }
        }
}