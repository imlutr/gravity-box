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

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Linearly interpolates to the target values. */
fun Vector3.lerp(targetX: Float, targetY: Float, targetZ: Float = 0f, progress: Float): Vector3 {
    x += progress * (targetX - x)
    y += progress * (targetY - y)
    z += progress * (targetZ - z)
    return this
}

/**
 * Used to compare a color that was linearly interpolated using lerp, resulting in imprecision.
 */
fun Color.approxEqualTo(color: Color): Boolean {
    return (Math.abs(this.r - color.r) <= 1 / 255f) && (Math.abs(this.g - color.g) <= 1 / 255f) && (Math.abs(
            this.b - color.b
    ) <= 1 / 255f)
}

/**
 * Sets [this] color to [color] without copying the alpha value to keep its transparency.
 */
fun Color.setWithoutAlpha(color: Color) {
    this.r = color.r
    this.g = color.g
    this.b = color.b
}

private val bodyArray: Array<Body> = Array()
val World.bodies: Array<Body>
    get() {
        getBodies(bodyArray)
        return bodyArray
    }

private var coords = Vector3()
fun screenToWorldCoordinates(screenX: Int, screenY: Int, gameCamera: GameCamera = Injekt.get()): Vector3 {
    coords.run {
        x = screenX.toFloat()
        y = screenY.toFloat()
    }
    gameCamera.unproject(coords)
    return coords
}

fun Stage.hitScreen(screenX: Int, screenY: Int, touchable: Boolean = true): Actor? {
    val stageCoords = screenToStageCoordinates(Vector2(screenX.toFloat(), screenY.toFloat()))
    return hit(stageCoords.x, stageCoords.y, touchable)
}

/**
 * Rounding to the nearest 10 starting from 0:
 */
fun Float.roundToNearest(x: Float, threshold: Float, startingValue: Float = 0f): Float {
    val valueRoundedDown = MathUtils.floor(this / x) * x
    val valueRoundedUp = MathUtils.ceil(this / x) * x
    return when {
        Math.abs((this + startingValue) - valueRoundedDown) < threshold -> valueRoundedDown - startingValue
        Math.abs((this + startingValue) - valueRoundedUp) < threshold -> valueRoundedUp - startingValue
        else -> this
    }
}