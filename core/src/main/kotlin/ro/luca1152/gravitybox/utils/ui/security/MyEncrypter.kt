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

import com.badlogic.gdx.utils.Pools

class MyEncrypter(private var encryptionSecretKey: String) {
    init {
        encryptionSecretKey = if (encryptionSecretKey == "") "encryptionSecretKey" else encryptionSecretKey
    }

    private fun encryptDecrypt(input: String): String {
        val outputBuilder = Pools.obtain(StringBuilder::class.java).clear()
        for (i in 0 until input.length) {
            val a: Int = input[i].toInt()
            val b: Int = encryptionSecretKey[i % encryptionSecretKey.length].toInt()
            outputBuilder.append((a xor b).toChar())
        }
        Pools.free(outputBuilder)
        return String(outputBuilder)
    }

    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    fun encrypt(string: String, signature: String): String {
        val signedString = "$signature|$string"
        val encryptedString = encryptDecrypt(signedString)
        return Hex.encode(encryptedString)
    }

    fun decrypt(string: String, signature: String): String {
        val decrypted = encryptDecrypt(Hex.decode(string))
        return if (decrypted.startsWith("$signature|")) {
            decrypted.substring(signature.length + 1)
        } else {
            error("The signatures don't match.")
        }
    }
}