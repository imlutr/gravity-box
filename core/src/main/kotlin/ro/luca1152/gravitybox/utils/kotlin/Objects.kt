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
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.inject.Context
import ro.luca1152.gravitybox.components.game.pixelsToMeters

/*
 * Used in dependency injection so I can inject more variables of the same type,
 * such as an UICamera and a GameCamera, both being OrthographicCameras.
 */

class GameCamera : OrthographicCamera(720.pixelsToMeters, 1280.pixelsToMeters)
class GameViewport(context: Context) : ExtendViewport(720.pixelsToMeters, 1280.pixelsToMeters, context.inject<GameCamera>())
class GameStage(context: Context) : Stage(context.inject<GameViewport>(), context.inject())

class OverlayCamera : OrthographicCamera()
class OverlayViewport(context: Context) : ExtendViewport(720f, 1280f, context.inject<OverlayCamera>())
class OverlayStage(context: Context) : Stage(context.inject<OverlayViewport>(), context.inject())

class UICamera : OrthographicCamera()
class UIViewport(context: Context) : ExtendViewport(720f, 1280f, context.inject<UICamera>())
class UIStage(context: Context) : Stage(context.inject<UIViewport>(), context.inject())

// The MenuOverlayStage is used for showing the menu that pops from the side when tapping the menu button
class MenuOverlayViewport(context: Context) : ExtendViewport(720f, 1280f, context.inject<UICamera>())

class MenuOverlayStage(context: Context) : Stage(context.inject<UIViewport>(), context.inject())

class DistanceFieldShader(vertexShader: String, fragmentShader: String) : ShaderProgram(vertexShader, fragmentShader)