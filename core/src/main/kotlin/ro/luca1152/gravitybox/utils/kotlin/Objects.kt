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

package ro.luca1152.gravitybox.utils.kotlin

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ro.luca1152.gravitybox.pixelsToMeters
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/* Used in dependency injection so I can inject more variables of the same type,
 * such as an UICamera and a GameCamera, both being OrthographicCamera's.
 */

object GameCamera : OrthographicCamera(720.pixelsToMeters, 1280.pixelsToMeters)
object GameViewport : ExtendViewport(720.pixelsToMeters, 1280.pixelsToMeters, GameCamera)
object GameStage : Stage(GameViewport, Injekt.get())

object OverlayCamera : OrthographicCamera()
object OverlayViewport : ExtendViewport(720f, 1280f, OverlayCamera)
object OverlayStage : Stage(OverlayViewport, Injekt.get())

object UICamera : OrthographicCamera()
object UIViewport : ExtendViewport(720f, 1280f, UICamera)
object UIStage : Stage(UIViewport, Injekt.get())