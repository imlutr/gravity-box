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

@file:JvmName("HeadlessLauncher")

package ro.luca1152.gravitybox.headless

import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import ro.luca1152.gravitybox.MyGame

/** Launches the headless application. Can be converted into a utilities project or a server application. */
fun main(args: Array<String>) {
    HeadlessApplication(MyGame(), HeadlessApplicationConfiguration().apply {
        renderInterval = -1f
    })
}
