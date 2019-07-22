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

import kotlin.experimental.and

/**
 * Helper methods for encode/decode hex strings.
 *
 * @author https://github.com/google/tink/blob/master/java/src/main/java/com/google/crypto/tink/subtle/Hex.java
 */
object Hex {
    /** Encodes a byte array to hex.  */
    fun encode(bytes: ByteArray): String {
        val chars = "0123456789abcdef"
        val result = StringBuilder(2 * bytes.size)
        for (b in bytes) {
            // convert to unsigned
            val value = b and 0xff.toByte()
            result.append(chars[value / 16])
            result.append(chars[value % 16])
        }
        return result.toString()
    }

    /** Decodes a hex string to a byte array.  */
    fun decode(hex: String): ByteArray {
        if (hex.length % 2 != 0) {
            throw IllegalArgumentException("Expected a string of even length")
        }
        val size = hex.length / 2
        val result = ByteArray(size)
        for (i in 0 until size) {
            val hi = Character.digit(hex[2 * i], 16)
            val lo = Character.digit(hex[2 * i + 1], 16)
            if (hi == -1 || lo == -1) {
                throw IllegalArgumentException("input is not hexadecimal")
            }
            result[i] = (16 * hi + lo).toByte()
        }
        return result
    }
}