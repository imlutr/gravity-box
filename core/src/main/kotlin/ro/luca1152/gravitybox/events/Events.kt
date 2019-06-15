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

package ro.luca1152.gravitybox.events

import com.badlogic.gdx.math.Interpolation

/** The base [Event] class. */
interface Event

class UpdateRoundedPlatformsEvent : Event

open class FadeOutFadeInEvent(
    val fadeOutDuration: Float, val fadeOutInterpolation: Interpolation,
    val fadeInDuration: Float, val fadeInInterpolation: Interpolation
) : Event {
    companion object {
        val CLEAR_ACTIONS = Float.NEGATIVE_INFINITY
    }
}

class FadeOutEvent(fadeOutDuration: Float, fadeOutInterpolation: Interpolation = Interpolation.linear) :
    FadeOutFadeInEvent(fadeOutDuration, fadeOutInterpolation, 0f, Interpolation.linear)

class FadeInEvent(fadeInDuration: Float, fadeInInterpolation: Interpolation = Interpolation.linear) :
    FadeOutFadeInEvent(0f, Interpolation.linear, fadeInDuration, fadeInInterpolation)
