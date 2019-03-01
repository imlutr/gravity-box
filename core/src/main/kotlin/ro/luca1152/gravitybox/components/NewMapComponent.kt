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

package ro.luca1152.gravitybox.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter
import com.badlogic.gdx.utils.Pool.Poolable
import ro.luca1152.gravitybox.components.utils.ComponentResolver
import ro.luca1152.gravitybox.components.utils.tryGet
import ro.luca1152.gravitybox.metersToPixels
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.StringWriter

@Suppress("PrivatePropertyName")
class NewMapComponent : Component, Poolable {
    companion object : ComponentResolver<NewMapComponent>(NewMapComponent::class.java) {
        const val GRAVITY = -25f
    }

    val world: World = Injekt.get()
    var widthInTiles = 0
    var heightInTiles = 0
    /**
     * The level number of the currently stored map. It may differ from the level intended
     * to be played stored in [LevelComponent].
     */
    var levelNumber = 0

    fun set(widthInTiles: Int, heightInTiles: Int) {
        this.widthInTiles = widthInTiles
        this.heightInTiles = heightInTiles
    }

    fun saveMap(engine: PooledEngine = Injekt.get()) {
        var player: Entity? = null
        var finishPoint: Entity? = null
        val platforms = Array<Entity>()
        engine.getEntitiesFor(Family.all(NewMapObjectComponent::class.java).get()).forEach {
            when {
                it.tryGet(PlayerComponent) != null -> {
                    check(player == null) { "A map can't have more than one player." }
                    player = it
                }
                it.tryGet(FinishComponent) != null -> {
                    check(finishPoint == null) { " A map can't have more than one finish point." }
                    finishPoint = it
                }
                it.tryGet(PlatformComponent) != null -> platforms.add(it)
            }
        }
        check(player != null) { "A map must have a player." }
        check(finishPoint != null) { "A map must have a finish point." }

        val json = Json()
        json.run {
            setOutputType(JsonWriter.OutputType.json)
            setWriter(JsonWriter(StringWriter()))
            writeObjectStart()

            // Map properties
            writeValue("id", levelNumber)
            writeValue("width", widthInTiles.metersToPixels)
            writeValue("height", heightInTiles.metersToPixels)

            // Objects
            player!!.json.writeToJson(this)
            finishPoint!!.json.writeToJson(this)
            writeArrayStart("objects")
            platforms.forEach {
                it.json.writeToJson(this)
            }
            writeArrayEnd()

            writeObjectEnd()
        }
        println(json.prettyPrint(json.writer.writer.toString()))
    }


    override fun reset() {
        destroyAllBodies()
        levelNumber = 0
    }

    private fun destroyAllBodies() {
        val bodiesToRemove = Array<Body>()
        world.getBodies(bodiesToRemove)
        bodiesToRemove.forEach {
            world.destroyBody(it)
        }
    }
}

val Entity.newMap: NewMapComponent
    get() = NewMapComponent[this]