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

    // Debug
    val CAN_LOAD_ANY_LEVEL = false
    val LOAD_SPECIFIC_LEVEL = -1

    // Rules
    val LEVEL_COUNT = 269
    val GRAVITY = -25f
    val IS_MOBILE = Gdx.app.type == Application.ApplicationType.Android || Gdx.app.type == Application.ApplicationType.iOS
    val ENABLE_LEVEL_EDITOR = !IS_MOBILE

    // Stats
    /** The highest level the player finished. */
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
    /** How many bullets did the player shot. */
    var BULLET_COUNT
        get() = preferences.getInteger("bulletCount", 0)
        set(value) {
            preferences.run {
                putInteger("bulletCount", value)
                flush()
            }
        }
    /** How many times did the player manually restart (by pressing the restart button, not by dying) a level*/
    var RESTART_COUNT
        get() = preferences.getInteger("restartCount", 0)
        set(value) {
            preferences.run {
                putInteger("restartCount", value)
                flush()
            }
        }
    /** How many times did the player die. */
    var DEATH_COUNT
        get() = preferences.getInteger("deathCount", 0)
        set(value) {
            preferences.run {
                putInteger("deathCount", value)
                flush()
            }
        }
    /** How many destroyable platforms did the player destroy. */
    var DESTROYED_PLATFORMS_COUNT
        get() = preferences.getInteger("destroyedPlatformsCount", 0)
        set(value) {
            preferences.run {
                putInteger("destroyedPlatformsCount", value)
                flush()
            }
        }
    /** How many points did the player collect. */
    var COLLECTED_POINT_COUNT
        get() = preferences.getInteger("collectedPointCount", 0)
        set(value) {
            preferences.run {
                putInteger("collectedPointCount", value)
                flush()
            }
        }

    // Finish game stats (the stats shown on the last level)
    /** True if the last level was reached. */
    var DID_FINISH_GAME
        get() = preferences.getBoolean("didFinishGame", false)
        set(value) {
            preferences.run {
                putBoolean("didFinishGame", value)
                flush()
            }
        }
    /** The time (in seconds) the player finished the game in. */
    var FINISH_TIME
        get() = preferences.getFloat("finishGameTime", 0f)
        set(value) {
            preferences.run {
                putFloat("finishGameTime", value)
                flush()
            }
        }
    /** How many bullets did the player shot until finishing the game. */
    var FINISH_BULLET_COUNT
        get() = preferences.getInteger("finishBulletsCount", 0)
        set(value) {
            preferences.run {
                putInteger("finishBulletsCount", value)
                flush()
            }
        }
    /** How many times did the player manually restart (by pressing the restart button, not by dying) a level until finishing the game. */
    var FINISH_RESTART_COUNT
        get() = preferences.getInteger("finishRestartCount", 0)
        set(value) {
            preferences.run {
                putInteger("finishRestartCount", value)
                flush()
            }
        }
    /** How many times did the player die until finishing the game. */
    var FINISH_DEATH_COUNT
        get() = preferences.getInteger("finishDeathCount", 0)
        set(value) {
            preferences.run {
                putInteger("finishDeathCount", value)
                flush()
            }
        }
    /** How many destroyable platforms did the player destroy until finishing the game. */
    var FINISH_DESTROYED_PLATFORM_COUNT
        get() = preferences.getInteger("finishDestroyedPlatformCount", 0)
        set(value) {
            preferences.run {
                putInteger("finishDestroyedPlatformCount", value)
                flush()
            }
        }
    /** How many points did the player collect until finishing the game. */
    var FINISH_COLLECTED_POINT_COUNT
        get() = preferences.getInteger("finishCollectedPointCount", 0)
        set(value) {
            preferences.run {
                putInteger("finishCollectedPointCount", value)
                flush()
            }
        }

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