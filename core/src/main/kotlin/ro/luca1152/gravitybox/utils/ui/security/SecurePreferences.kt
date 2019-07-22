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
import ro.luca1152.gravitybox.utils.kotlin.injectNullable

@Suppress("LibGDXMissingFlush")
/**
 * My implementation of [Preferences] which stores data encrypted using an XOR algorithm. Not meant to be very secure,
 * as it is meant mainly to stop people from altering the preferences if they have rooted phones, while being very fast.
 */
class SecurePreferences(context: Context) {
    // Injected objects
    private val myEncrypter: MyEncrypter? = context.injectNullable()

    private val preferences = Gdx.app.getPreferences("Gravity Box by Luca1152")

    fun get() = preferences.get()!!
    fun flush() = preferences.flush()
    fun clear() = preferences.clear()

    fun putString(key: String, value: String) {
        if (myEncrypter == null) preferences.putString(key, value)
        else preferences.putString(key, myEncrypter.encrypt(value, key))
    }

    fun getString(key: String, defaultValue: String): String {
        return if (myEncrypter == null) preferences.getString(key, defaultValue)
        else {
            if (preferences.contains(key)) {
                try {
                    myEncrypter.decrypt(preferences.getString(key), key)
                } catch (e: Throwable) {
                    return defaultValue
                }
            } else return defaultValue
        }
    }

    fun putInteger(key: String, value: Int) {
        if (myEncrypter == null) preferences.putInteger(key, value)
        else preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getInteger(key: String, defaultValue: Int): Int {
        return if (myEncrypter == null) preferences.getInteger(key, defaultValue)
        else {
            if (preferences.contains(key)) {
                try {
                    myEncrypter.decrypt(preferences.getString(key), key).toInt()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    return defaultValue
                }
            } else return defaultValue
        }
    }

    fun putFloat(key: String, value: Float) {
        if (myEncrypter == null) preferences.putFloat(key, value)
        else preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return if (myEncrypter == null) preferences.getFloat(key, defaultValue)
        else {
            if (preferences.contains(key)) {
                try {
                    myEncrypter.decrypt(preferences.getString(key), key).toFloat()
                } catch (e: Throwable) {
                    return defaultValue
                }
            } else return defaultValue
        }
    }

    fun putLong(key: String, value: Long) {
        if (myEncrypter == null) preferences.putLong(key, value)
        else preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return if (myEncrypter == null) preferences.getLong(key, defaultValue)
        else {
            if (preferences.contains(key)) {
                try {
                    myEncrypter.decrypt(preferences.getString(key), key).toLong()
                } catch (e: Throwable) {
                    return defaultValue
                }
            } else return defaultValue
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        if (myEncrypter == null) preferences.putBoolean(key, value)
        else preferences.putString(key, myEncrypter.encrypt(value.toString(), key))
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (myEncrypter == null) preferences.getBoolean(key, defaultValue)
        else {
            if (preferences.contains(key)) {
                try {
                    myEncrypter.decrypt(preferences.getString(key), key).toBoolean()
                } catch (e: Throwable) {
                    return defaultValue
                }
            } else return defaultValue
        }
    }
}