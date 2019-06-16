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

package ro.luca1152.gravitybox.ios

import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration

import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication

import ro.luca1152.gravitybox.MyGame

/** Launches the iOS (RoboVM) application. */
class IOSLauncher : IOSApplication.Delegate() {
    override fun createApplication(): IOSApplication {
        val configuration = IOSApplicationConfiguration().apply {
            orientationLandscape = false
            orientationPortrait = true
            preventScreenDimming = true
        }
        return IOSApplication(MyGame(), configuration)
    }

    companion object {
        @JvmStatic
        fun main(argv: Array<String>) {
            val pool = NSAutoreleasePool()
            UIApplication.main<UIApplication, IOSLauncher>(argv, null, IOSLauncher::class.java)
            pool.close()
        }
    }
}