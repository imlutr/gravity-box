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
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pools
import ktx.app.KtxGame
import ktx.app.clearScreen
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver

/** Linearly interpolates to the target values. */
fun Vector3.lerp(targetX: Float, targetY: Float, targetZ: Float = 0f, progress: Float): Vector3 {
    x += progress * (targetX - x)
    y += progress * (targetY - y)
    z += progress * (targetZ - z)
    return this
}

/** Used to compare a color that was linearly interpolated using lerp, resulting in imprecision. */
fun Color.approxEqualTo(color: Color): Boolean {
    return (Math.abs(this.r - color.r) <= 3 / 255f) && (Math.abs(this.g - color.g) <= 3 / 255f) && (Math.abs(this.b - color.b) <= 3 / 255f)
}

fun Color.setWithoutAlpha(color: Color) {
    this.r = color.r
    this.g = color.g
    this.b = color.b
}

fun screenToWorldCoordinates(context: Context, screenX: Int, screenY: Int): Vector3 {
    val gameCamera: GameCamera = context.inject()
    val coords = Vector3(screenX.toFloat(), screenY.toFloat(), 0f)
    gameCamera.unproject(coords)
    return coords
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

inline fun <reified T : Component> Engine.getNullableSingleton(): Entity? {
    val entitiesFound = getEntitiesFor(Family.all(T::class.java).get())
    check(entitiesFound.size() <= 1) { "A singleton can't be instantiated more than once." }
    return when (entitiesFound.size()) {
        1 -> entitiesFound.first()
        else -> null
    }
}

inline fun <reified T : Component> Engine.getSingleton(): Entity {
    val entity = getNullableSingleton<T>()
    check(entity != null) { "No singleton found for the given component." }
    return entity
}

inline fun <reified T : Component> createComponent(context: Context): T {
    val engine: PooledEngine = context.inject()
    return engine.createComponent(T::class.java)
}

inline fun <reified T : Component> Entity.removeComponent() {
    remove(T::class.java)
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
                val localPosition = Pools.obtain(Vector2::class.java).set(localX, localY)
                val coords = this.parentToLocalCoordinates(this@hitAll.localToStageCoordinates(localPosition))
                hit(coords.x, coords.y, touchable)?.let {
                    hitActors.add(it)
                }
                Pools.free(localPosition)
            }
        }
    }
    return hitActors
}

fun Stage.hitAll(stageX: Float, stageY: Float, touchable: Boolean = true): Array<Actor> {
    val hitActors = Array<Actor>()
    actors.forEach { child ->
        child.run {
            val localPosition = Pools.obtain(Vector2::class.java).set(stageX, stageY)
            val coords = this.parentToLocalCoordinates(localPosition)
            hit(coords.x, coords.y, touchable)?.let {
                hitActors.add(it)
            }
            Pools.free(localPosition)
        }
    }
    return hitActors
}

fun Stage.hitAllScreen(screenX: Int, screenY: Int, touchable: Boolean = true): Array<Actor> {
    val stageCoords = screenToStageCoordinates(Vector2(screenX.toFloat(), screenY.toFloat()))
    return hitAll(stageCoords.x, stageCoords.y, touchable)
}

val Polygon.leftmostX: Float
    get() = transformedVertices.filterIndexed { index, _ -> index % 2 == 0 }.sorted().first()

val Polygon.rightmostX: Float
    get() = transformedVertices.filterIndexed { index, _ -> index % 2 == 0 }.sorted().last()

val Polygon.bottommostY: Float
    get() = transformedVertices.filterIndexed { index, _ -> index % 2 == 1 }.sorted().first()

val Polygon.topmostY: Float
    get() = transformedVertices.filterIndexed { index, _ -> index % 2 == 1 }.sorted().last()

fun Float.approximatelyEqualTo(fl: Float) = Math.abs(this - fl) <= 1e-5f

inline fun <T> Iterable<T>.filterNullableSingleton(predicate: (T) -> Boolean): T? {
    val filteredList = filterTo(ArrayList(), predicate)
    check(filteredList.size <= 1) { "A singleton can't be instantiated more than once" }
    return when {
        filteredList.size == 1 -> filteredList.first()
        else -> null
    }
}

fun Entity.addToEngine(context: Context): Entity {
    val engine: PooledEngine = context.inject()

    engine.addEntity(this)
    return this
}

fun newEntity(context: Context) = context.inject<PooledEngine>().createEntity()!!

inline fun <reified Type : Any> Context.injectNullable(): Type? = if (contains<Type>()) getProvider(Type::class.java)() else null