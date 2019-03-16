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

import com.badlogic.ashley.core.*
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import ktx.app.KtxGame
import ktx.app.clearScreen
import ro.luca1152.gravitybox.utils.components.ComponentResolver
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Linearly interpolates to the target values. */
fun Vector3.lerp(targetX: Float, targetY: Float, targetZ: Float = 0f, progress: Float): Vector3 {
    x += progress * (targetX - x)
    y += progress * (targetY - y)
    z += progress * (targetZ - z)
    return this
}

/** Used to compare a color that was linearly interpolated using lerp, resulting in imprecision. */
fun Color.approxEqualTo(color: Color): Boolean {
    return (Math.abs(this.r - color.r) <= 1 / 255f) && (Math.abs(this.g - color.g) <= 1 / 255f) && (Math.abs(this.b - color.b) <= 1 / 255f)
}

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

fun screenToWorldCoordinates(screenX: Int, screenY: Int, gameCamera: GameCamera = Injekt.get()): Vector3 {
    val coords = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
    gameCamera.unproject(coords)
    return coords
}

fun Stage.hitScreen(screenX: Int, screenY: Int, touchable: Boolean = true): Actor? {
    val stageCoords = screenToStageCoordinates(Vector2(screenX.toFloat(), screenY.toFloat()))
    return hit(stageCoords.x, stageCoords.y, touchable)
}

fun Float.roundToNearest(nearest: Float, threshold: Float, startingValue: Float = 0f): Float {
    val valueRoundedDown = MathUtils.floor(this / nearest) * nearest
    val valueRoundedUp = MathUtils.ceil(this / nearest) * nearest
    return when {
        Math.abs((this + startingValue) - valueRoundedDown) < threshold -> valueRoundedDown - startingValue
        Math.abs((this + startingValue) - valueRoundedUp) < threshold -> valueRoundedUp - startingValue
        else -> this
    }
}

fun Polygon.getRectangleCenter(): Vector2 {
    require(vertices.size == 4 * 2) { "The Polygon given is not a rectangle." }

    val vertices = transformedVertices
    return Vector2((vertices[0] + vertices[4]) / 2f, (vertices[1] + vertices[5]) / 2f)
}

fun Engine.getNullableSingletonFor(family: Family): Entity? {
    val entitiesFound = getEntitiesFor(family)
    check(entitiesFound.size() <= 1) { "A singleton can't be instantiated more than once." }
    return when (entitiesFound.size()) {
        1 -> entitiesFound.first()
        else -> null
    }
}

/**
 * Returns the first entity of [Engine.getEntitiesFor].
 * If there is more than one or no Entity found, [IllegalStateException] is thrown.
 */
fun Engine.getSingletonFor(family: Family): Entity {
    val entity = getNullableSingletonFor(family)
    check(entity != null) { "No singleton found for the given Family. " }
    return entity
}

fun Engine.removeAllSystems(except: ArrayList<Class<out EntitySystem>> = ArrayList()) {
    val systemsToRemove = Array<EntitySystem>()
    systems.forEach {
        if (!except.contains(it.javaClass))
            systemsToRemove.add(it)
    }
    systemsToRemove.forEach {
        it.engine.removeSystem(it)
    }
}

fun Engine.clear() {
    removeAllEntities()
    removeAllSystems()
}

/**
 * Returns the component if the has the [componentResolver].
 * Otherwise, it returns null.
 */
fun <T : Component> Entity.tryGet(componentResolver: ComponentResolver<T>): T? = componentResolver[this]

/** Removes the [entity] from the engine and resets each of its components. */
fun Engine.removeAndResetEntity(entity: Entity) {
    // Reset every component so you don't have to manually reset them for
    // each entity, such as calling world.destroyBody(entity.body.body).
    for (component in entity.components) {
        if (component is Pool.Poolable)
            component.reset()
        entity.remove(component::class.java)
    }

    // Call the default removeEntity() function
    this.removeEntity(entity)
}

fun <Type : Screen> KtxGame<Type>.setScreen(screen: Type) {
    if (containsScreen(screen.javaClass)) {
        removeScreen(screen.javaClass)
    }
    addScreen(screen.javaClass, screen)
    setScreen(screen.javaClass)
}

fun clearScreen(color: Color) {
    clearScreen(color.r, color.g, color.b, 1f)
}

/**
 * Returns the [Actor] at the specified location in local coordinates.
 */
fun Actor.hitAll(localX: Float, localY: Float, touchable: Boolean = false): Array<Actor> {
    val hitActors = Array<Actor>()
    stage.actors.forEach { child ->
        if (child != this) {
            child.run {
                val coords = this.parentToLocalCoordinates(this@hitAll.localToStageCoordinates(Vector2(localX, localY)))
                hit(coords.x, coords.y, touchable)?.let {
                    hitActors.add(it)
                }
            }
        }
    }
    return hitActors
}
