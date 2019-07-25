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

package ro.luca1152.gravitybox

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import ktx.inject.Context
import ro.luca1152.gravitybox.utils.ui.security.SecurePreferences

@Suppress("LibGDXMissingFlush", "SpellCheckingInspection", "PropertyName", "MemberVisibilityCanBePrivate")
class GameRules(context: Context) {
    private val preferences: SecurePreferences = context.inject()

    /** Makes sure updates are persisted. */
    fun flushUpdates() {
        preferences.flush()
    }

    // Debug
    val CAN_PLAY_ANY_LEVEL = false
    val PLAY_SPECIFIC_LEVEL = -1

    // Encryption
    var ARE_RULES_ENCRYPTED
        get() = preferences.getBoolean("areRulesEncrypted", false)
        set(value) {
            preferences.putBoolean("areRulesEncrypted", value)
        }

    // Ban
    /**
     * True if the player is soft banned.
     * Soft ban means that the player can read from the shots leaderboard, but can't write in it.
     * So the gameplay would be exactly the same from the player's perspective.
     *
     * A player is soft banned if he tried to alter the number of shots so he'd get rank #1, but also if he is
     * running the game on a rooted Android phone (by default, just in case). Sorry rooted users!
     */
    var IS_PLAYER_SOFT_BANNED
        get() = preferences.getBoolean("isPlayerSoftBanned", false)
        set(value) {
            preferences.putBoolean("isPlayerSoftBanned", value)
        }

    // Rules
    /**
     * The game levels' version. If they change in the future (their order/I add new ones),
     * then the leaderboard would likely have to be updated as well.
     */
    val GAME_LEVELS_VERSION = "v1"
    /** How many levels the game has. */
    val LEVEL_COUNT = 269
    /** The Box2D World's gravity. */
    val GRAVITY = -25f
    /** True if the device runs Android. */
    val IS_ANDROID = Gdx.app.type == Application.ApplicationType.Android
    /** True if the device runs iOS. */
    val IS_IOS = Gdx.app.type == Application.ApplicationType.iOS
    /** True if the device runs Android or iOS. */
    val IS_MOBILE = IS_ANDROID || IS_IOS
    /** If true, the level editor button is shown. */
    val ENABLE_LEVEL_EDITOR = !IS_MOBILE
    /** The minimum delay in seconds between two shots. */
    val TIME_DELAY_BETWEEN_SHOTS = 0.05f

    // Stats
    /** The highest level the player finished. */
    var HIGHEST_FINISHED_LEVEL
        get() = preferences.getInteger("highestFinishedLevel", 0)
        set(value) {
            preferences.putInteger("highestFinishedLevel", value)
        }

    /** The time (in seconds) a user spent playing the game. */
    var PLAY_TIME
        get() = preferences.getFloat("playTime", 0f)
        set(value) {
            preferences.putFloat("playTime", value)
        }
    /** How many bullets did the player shot. */
    var BULLET_COUNT
        get() = preferences.getInteger("bulletCount", 0)
        set(value) {
            preferences.putInteger("bulletCount", value)
        }
    /** How many times did the player manually restart (by pressing the restart button, not by dying) a level*/
    var RESTART_COUNT
        get() = preferences.getInteger("restartCount", 0)
        set(value) {
            preferences.putInteger("restartCount", value)
        }
    /** How many times did the player die. */
    var DEATH_COUNT
        get() = preferences.getInteger("deathCount", 0)
        set(value) {
            preferences.putInteger("deathCount", value)
        }
    /** How many destroyable platforms did the player destroy. */
    var DESTROYED_PLATFORMS_COUNT
        get() = preferences.getInteger("destroyedPlatformsCount", 0)
        set(value) {
            preferences.putInteger("destroyedPlatformsCount", value)
        }
    /** How many points did the player collect. */
    var COLLECTED_POINT_COUNT
        get() = preferences.getInteger("collectedPointCount", 0)
        set(value) {
            preferences.putInteger("collectedPointCount", value)
        }

    // Finish game stats (the stats shown on the last level)
    /** True if the last level was reached. */
    var DID_FINISH_GAME
        get() = preferences.getBoolean("didFinishGame", false)
        set(value) {
            preferences.putBoolean("didFinishGame", value)
        }
    /** The time (in seconds) the player finished the game in. */
    var FINISH_TIME
        get() = preferences.getFloat("finishGameTime", 0f)
        set(value) {
            preferences.putFloat("finishGameTime", value)
        }
    /** How many bullets did the player shot until finishing the game. */
    var FINISH_BULLET_COUNT
        get() = preferences.getInteger("finishBulletsCount", 0)
        set(value) {
            preferences.putInteger("finishBulletsCount", value)
        }
    /** How many times did the player manually restart (by pressing the restart button, not by dying) a level until finishing the game. */
    var FINISH_RESTART_COUNT
        get() = preferences.getInteger("finishRestartCount", 0)
        set(value) {
            preferences.putInteger("finishRestartCount", value)
        }
    /** How many times did the player die until finishing the game. */
    var FINISH_DEATH_COUNT
        get() = preferences.getInteger("finishDeathCount", 0)
        set(value) {
            preferences.putInteger("finishDeathCount", value)
        }
    /** How many destroyable platforms did the player destroy until finishing the game. */
    var FINISH_DESTROYED_PLATFORM_COUNT
        get() = preferences.getInteger("finishDestroyedPlatformCount", 0)
        set(value) {
            preferences.putInteger("finishDestroyedPlatformCount", value)
        }
    /** How many points did the player collect until finishing the game. */
    var FINISH_COLLECTED_POINT_COUNT
        get() = preferences.getInteger("finishCollectedPointCount", 0)
        set(value) {
            preferences.putInteger("finishCollectedPointCount", value)
        }

    // Rate-related
    val MIN_FINISHED_LEVELS_TO_SHOW_RATE_PROMPT = 13
    var DID_RATE_THE_GAME
        get() = preferences.getBoolean("didRateGame", false)
        set(value) {
            preferences.putBoolean("didRateGame", value)
        }
    /** True if the player pressed the `Never` button when asked to rate the game. */
    var NEVER_PROMPT_USER_TO_RATE_THE_GAME
        get() = preferences.getBoolean("neverPromptUserToRate", false)
        set(value) {
            preferences.putBoolean("neverPromptUserToRate", value)
        }
    val TIME_DELAY_BETWEEN_PROMPTING_USER_TO_RATE_THE_GAME_AGAIN = 5f * 60 // 5 minutes
    /** The player will be asked to rate the game again after the PLAY_TIME exceeds this value if he chose to rate the game `Later`.*/
    var MIN_PLAY_TIME_TO_PROMPT_USER_TO_RATE_THE_GAME_AGAIN
        get() = preferences.getFloat("minPlayTimeToPromptUserToRateTheGameAgain", 0f)
        set(value) {
            preferences.putFloat("minPlayTimeToPromptUserToRateTheGameAgain", value)
        }

    // Ads
    /** Is true after any donation. */
    var IS_AD_FREE
        get() = preferences.getBoolean("isAdFree", false)
        set(value) {
            preferences.putBoolean("isAdFree", value)
        }

    // Interstitial ads
    /** There must be a delay of at least 3 levels between two interstitial ads. */
    val LEVELS_DELAY_BETWEEN_INTERSTITIAL_ADS = 3
    /** The first interstitial will be shown after 2 minutes. */
    val TIME_DELAY_BEFORE_SHOWING_FIRST_INTERSTITIAL_AD = 2f * 60
    /** There must be a delay of at least 3.75 minutes between two interstitial ads. */
    val TIME_DELAY_BETWEEN_INTERSTITIAL_ADS = 3.75f * 60
    /**
     * True when an ad should be shown.
     * Set true by the InterstitialAdsSystem.
     * Set false by the LevelFinishSystem.
     */
    var SHOULD_SHOW_INTERSTITIAL_AD = false

    // Rewarded ads
    /** There must be a delay of at least 5 minutes between two rewarded ads. */
    val TIME_DELAY_BETWEEN_REWARDED_ADS = 5f * 60
    /**
     * The time in seconds until a rewarded ad can be shown.
     * Is kept in [Preferences] so the value is kept between restarts.
     */
    var TIME_UNTIL_REWARDED_AD_CAN_BE_SHOWN
        get() = preferences.getFloat("timeUntilRewardedAdCanBeShown", 0f)
        set(value) {
            preferences.putFloat("timeUntilRewardedAdCanBeShown", value)
        }

    // Leaderboard
    /** The default value returned when reading a non-existent highscore using [getGameLevelHighscore]. */
    val DEFAULT_HIGHSCORE_VALUE = Int.MAX_VALUE
    /** The score stored in the [Preferences] to mark that the level was skipped. */
    val SKIPPED_LEVEL_SCORE_VALUE = -1

    /** Used for avoiding excessive [String] allocations. */
    private val gameLevelHighscoreKeys = (1..LEVEL_COUNT).associateWith { "game${it}Highscore" }

    /** Returns the least number of shots the game (not community) level [level] was finished in. */
    fun getGameLevelHighscore(level: Int) = preferences.getInteger(gameLevelHighscoreKeys.getValue(level), Int.MAX_VALUE)

    /** Sets the least number of shots the game (not community) level [level] was finished in. */
    fun setGameLevelHighscore(level: Int, highscore: Int) {
        if (getGameLevelHighscore(level) > highscore) {
            preferences.putInteger(gameLevelHighscoreKeys.getValue(level), highscore)
        } else {
            Gdx.app.log("WARNING", "Tried to set the highscore to a worse value.")
        }
    }

    /** Returs true if the given [level] was skipped. */
    fun isGameLevelSkipped(level: Int) = getGameLevelHighscore(level) == SKIPPED_LEVEL_SCORE_VALUE

    /** The delay in miliseconds between leaderboard caches to storage. */
    val TIME_DELAY_BETWEEN_CACHING_LEADERBOARD = 12L * 3600 * 1000

    /** The time in seconds until the entire leaderboard is cached to storage. */
    var NEXT_LEADERBOARD_CACHE_TIME
        get() = preferences.getLong("nextLeaderboardCacheTime", 0L)
        set(value) {
            preferences.putLong("nextLeaderboardCacheTime", value)
        }

    /** The version of the leaderboard cached to storage. */
    var CACHED_LEADERBOARD_VERSION
        get() = preferences.getString("cachedLeaderboardVersion", GAME_LEVELS_VERSION)
        set(value) {
            preferences.putString("cachedLeaderboardVersion", value)
        }

    /** True if the `Tap anywhere to proceed` guide was shown between levels. */
    var DID_SHOW_GUIDE_BETWEEN_LEVELS
        get() = preferences.getBoolean("didShowGuideBetweenLevels", false)
        set(value) {
            preferences.putBoolean("didShowGuideBetweenLevels", value)
        }

    /** The default value returned when reading a non-existent highscore using [getGameLevelHighscore]. */
    val DEFAULT_RANK_VALUE = Int.MAX_VALUE

    /** Used for avoiding excessive [String] allocations. */
    private val gameLevelRankKeys = (1..LEVEL_COUNT).associateWith { "game${it}Rank" }

    /** Returns the [level]'s rank. */
    fun getGameLevelRank(level: Int) = preferences.getInteger(gameLevelRankKeys.getValue(level), DEFAULT_RANK_VALUE)

    /** Sets the [level]'s rank. */
    fun setGameLevelRank(level: Int, rank: Int) {
        if (getGameLevelRank(level) > rank) {
            preferences.putInteger(gameLevelRankKeys.getValue(level), rank)
        } else {
            Gdx.app.log("WARNING", "Tried to set the highscore to a worse value.")
        }
    }

    /** Returns true if the given [level] is unranked. */
    fun isGameLevelUnranked(level: Int) = getGameLevelRank(level) == DEFAULT_RANK_VALUE

    val DEFAULT_RANK_PERCENTAGE_VALUE = -1f

    /** Used for avoiding excessive [String] allocations. */
    private val gameLevelRankPercentageKeys = (1..LEVEL_COUNT).associateWith { "game${it}RankPercentage" }

    /** Returns the [level]'s rank top percentage. */
    fun getGameLevelRankPercentage(level: Int) = preferences.getFloat(
        gameLevelRankPercentageKeys.getValue(level), DEFAULT_RANK_PERCENTAGE_VALUE
    )

    /** Sets the [level]'s rank top percentage */
    fun setGameLevelRankPercentage(level: Int, percent: Float) {
        preferences.putFloat(gameLevelRankPercentageKeys.getValue(level), percent)
    }


    // Analytics
    /** Used for avoiding excessive [String] allocations. */
    private val gameLevelPlayTimeKeys = (1..LEVEL_COUNT).associateWith { "game${it}PlayTime" }

    /** Returns how much time a player spent playing the given level. */
    fun getGameLevelPlayTime(level: Int) = preferences.getFloat(gameLevelPlayTimeKeys.getValue(level), 0f)

    /** Sets how much time a player spent playing the given level. */
    fun setGameLevelPlayTime(level: Int, time: Float) {
        preferences.putFloat(gameLevelPlayTimeKeys.getValue(level), time)
    }

    /** Used for avoiding excessive [String] allocations. */
    private val gameLevelFinishCountKeys = (1..LEVEL_COUNT).associateWith { "game${it}FinishCount" }

    /** Returns how many times a player finished the given level. */
    fun getGameLevelFinishCount(level: Int) = preferences.getInteger(gameLevelFinishCountKeys.getValue(level), 0)

    /** Sets how many time a player finishet the given level. */
    fun setGameLevelFinishCount(level: Int, count: Int) {
        preferences.putInteger(gameLevelFinishCountKeys.getValue(level), count)
    }
}