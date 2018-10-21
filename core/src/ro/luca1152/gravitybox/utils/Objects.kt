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

package ro.luca1152.gravitybox.utils

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ro.luca1152.gravitybox.pixelsToMeters

/**
 * Used in dependency injection so I can inject more variables of the same type,
 * such as an UI camera and a [GameCamera], both being [OrthographicCamera].
 */

object GameCamera : OrthographicCamera(720.pixelsToMeters, 1280.pixelsToMeters)

object GameViewport : ExtendViewport(720.pixelsToMeters, 1280.pixelsToMeters, GameCamera)