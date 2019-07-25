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

package ro.luca1152.gravitybox.utils.ui.security

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import ktx.inject.Context

@Suppress("LibGDXMissingFlush")
/**
 * My implementation of [Preferences] which stores data encrypted using an XOR algorithm. Not meant to be very secure,
 * as it is meant mainly to stop people from altering the preferences if they have rooted phones, while being very fast.
 */
class SecurePreferences(context: Context) {
    // Injected objects
    private val myEncrypter: MyEncrypter = context.inject()

    private val preferences = Gdx.app.getPreferences("Gravity Box by Luca1152")

    // Getting values from the preferences unnecessarily consumes much memory, as it has to decode them
    // So it'd be better if the values were cached in a map, and taken from here when needed
    private val cachedValues = mutableMapOf<String, Any>()

    fun get() = preferences.get()!!
    fun flush() = preferences.flush()
    fun clear() = preferences.clear()

    fun putString(key: String, value: String, updateCache: Boolean = true) {
        if (updateCache) {
            cachedValues[key] = value
        }
        preferences.putString(key, myEncrypter.encrypt(value, key))
    }

    fun getString(key: String, defaultValue: String): String {
        return if (cachedValues.containsKey(key)) cachedValues[key] as String
        else {
            return if (preferences.contains(key)) {
                try {
                    val retrievedValue = myEncrypter.decrypt(preferences.getString(key), key)
                    cachedValues[key] = retrievedValue
                    retrievedValue
                } catch (e: Throwable) {
                    cachedValues[key] = defaultValue
                    putString(key, defaultValue)
                    defaultValue
                }
            } else defaultValue
        }
    }

    fun putInteger(key: String, value: Int) {
        cachedValues[key] = value
        preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getInteger(key: String, defaultValue: Int): Int {
        return if (cachedValues.containsKey(key)) cachedValues[key] as Int
        else {
            return if (preferences.contains(key)) {
                try {
                    val retrievedValue = myEncrypter.decrypt(preferences.getString(key), key).toInt()
                    cachedValues[key] = retrievedValue
                    retrievedValue
                } catch (e: Throwable) {
                    cachedValues[key] = defaultValue
                    putInteger(key, defaultValue)
                    defaultValue
                }
            } else defaultValue
        }
    }

    fun putFloat(key: String, value: Float) {
        cachedValues[key] = value
        preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return if (cachedValues.containsKey(key)) cachedValues[key] as Float
        else {
            return if (preferences.contains(key)) {
                try {
                    val retrievedValue = myEncrypter.decrypt(preferences.getString(key), key).toFloat()
                    cachedValues[key] = retrievedValue
                    retrievedValue
                } catch (e: Throwable) {
                    cachedValues[key] = defaultValue
                    putFloat(key, defaultValue)
                    defaultValue
                }
            } else defaultValue
        }
    }

    fun putLong(key: String, value: Long) {
        cachedValues[key] = value
        preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return if (cachedValues.containsKey(key)) cachedValues[key] as Long
        else {
            return if (preferences.contains(key)) {
                try {
                    val retrievedValue = myEncrypter.decrypt(preferences.getString(key), key).toLong()
                    cachedValues[key] = retrievedValue
                    retrievedValue
                } catch (e: Throwable) {
                    cachedValues[key] = defaultValue
                    putLong(key, defaultValue)
                    defaultValue
                }
            } else defaultValue
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        cachedValues[key] = value
        preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (cachedValues.containsKey(key)) cachedValues[key] as Boolean
        else {
            return if (preferences.contains(key)) {
                try {
                    val retrievedValue = myEncrypter.decrypt(preferences.getString(key), key).toBoolean()
                    cachedValues[key] = retrievedValue
                    retrievedValue
                } catch (e: Throwable) {
                    cachedValues[key] = defaultValue
                    putBoolean(key, defaultValue)
                    defaultValue
                }
            } else defaultValue
        }
    }
}