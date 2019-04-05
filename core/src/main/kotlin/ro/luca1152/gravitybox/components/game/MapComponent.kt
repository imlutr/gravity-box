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

package ro.luca1152.gravitybox.components.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter
import com.badlogic.gdx.utils.Pool.Poolable
import com.badlogic.gdx.utils.TimeUtils
import ktx.collections.sortBy
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.editor.json
import ro.luca1152.gravitybox.engine
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.utils.assets.json.*
import ro.luca1152.gravitybox.utils.assets.loaders.Text
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/** Pixels per meter. */
const val PPM = 64f

val Int.pixelsToMeters: Float
    get() = this / PPM

val Float.pixelsToMeters: Float
    get() = this / PPM

val Float.metersToPixels: Float
    get() = this * PPM

@Suppress("PrivatePropertyName")
/** Contains map information. */
class MapComponent : Component, Poolable {
    companion object : ComponentResolver<MapComponent>(MapComponent::class.java) {
        const val GRAVITY = -25f
    }

    var levelId = 1
    var mapLeft = Float.POSITIVE_INFINITY
    var mapRight = Float.NEGATIVE_INFINITY
    var mapBottom = Float.POSITIVE_INFINITY
    var mapTop = Float.NEGATIVE_INFINITY
    var updateRoundedPlatforms = true
    var paddingLeft = 2f
    var paddingRight = 2f
    var paddingTop = 5f
    var paddingBottom = 5f

    fun set(levelId: Int) {
        this.levelId = levelId
    }

    fun updateMapBounds(engine: PooledEngine = Injekt.get()) {
        mapLeft = Float.POSITIVE_INFINITY
        mapRight = Float.NEGATIVE_INFINITY
        mapBottom = Float.POSITIVE_INFINITY
        mapTop = Float.NEGATIVE_INFINITY
        engine.getEntitiesFor(Family.all(PolygonComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                it.polygon.run {
                    mapLeft = Math.min(mapLeft, leftmostX)
                    mapRight = Math.max(mapRight, rightmostX)
                    mapBottom = Math.min(mapBottom, bottommostY)
                    mapTop = Math.max(mapTop, topmostY)
                }
            }
        }
    }

    /** @param forceSave If true, it will override any previous map, even if the new file's content is the same. */
    fun saveMap(forceSave: Boolean = false) {
        val json = getJsonFromMap()
        writeJsonToFile(json, forceSave)
    }

    private fun getJsonFromMap(engine: PooledEngine = Injekt.get()): Json {
        var player: Entity? = null
        var finishPoint: Entity? = null
        val platforms = Array<Entity>()
        engine.getEntitiesFor(Family.all(MapObjectComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                when {
                    it.tryGet(PlayerComponent) != null -> {
                        check(player == null) { "A map can't have more than one player." }
                        player = it
                    }
                    it.tryGet(FinishComponent) != null -> {
                        check(finishPoint == null) { " A map can't have more than one finish point." }
                        finishPoint = it
                    }
                    it.tryGet(PlatformComponent) != null || it.tryGet(DestroyablePlatformComponent) != null -> {
                        platforms.add(it)
                    }
                }
            }
        }
        check(player != null) { "A map must have a player." }
        check(finishPoint != null) { "A map must have a finish point." }

        return Json().apply {
            setOutputType(JsonWriter.OutputType.json)
            setWriter(JsonWriter(StringWriter()))
            writeObjectStart()

            // Map properties
            writeValue("id", levelId)
            writeObjectStart("padding")
            writeValue("left", paddingLeft)
            writeValue("right", paddingRight)
            writeValue("top", paddingTop)
            writeValue("bottom", paddingBottom)
            writeObjectEnd()

            // Objects
            player!!.json.writeToJson(this)
            finishPoint!!.json.writeToJson(this)
            writeArrayStart("objects")
            platforms.sortBy { it.mapObject.id }
            platforms.forEach {
                it.json.writeToJson(this)
            }
            writeArrayEnd()

            writeObjectEnd()
        }
    }

    private fun writeJsonToFile(json: Json, forceSave: Boolean) {
        val fileFolder = "maps/editor"
        val existentFileName = getMapFileNameForId(levelId)
        if (existentFileName != "") {
            val oldJson = Gdx.files.local("$fileFolder/$existentFileName").readString()
            if (!forceSave && oldJson == json.prettyPrint(json.writer.writer.toString())) {
                return
            } else {
                Gdx.files.local("$fileFolder/$existentFileName").delete()
            }
        }
        val fileHandle = Gdx.files.local("$fileFolder/${getNewFileName()}")
        fileHandle.writeString(json.prettyPrint(json.writer.writer.toString()), false)
    }

    fun loadMap(
        mapFactory: MapFactory,
        playerEntity: Entity, finishEntity: Entity
    ) {
        destroyAllBodies()
        removePlatforms()
        createMap(mapFactory.id, mapFactory.padding)
        createPlayer(mapFactory.player, playerEntity)
        createFinish(mapFactory.finish, finishEntity)
        createPlatforms(mapFactory.objects)
        updateMapBounds()
    }

    private fun removePlatforms(engine: PooledEngine = Injekt.get()) {
        val entitiesToRemove = Array<Entity>()
        engine.getEntitiesFor(Family.all(PlatformComponent::class.java).get()).forEach {
            entitiesToRemove.add(it)
        }
        entitiesToRemove.forEach {
            engine.removeEntity(it)
        }
    }

    private fun createMap(id: Int, padding: PaddingPrototype) {
        levelId = id
        paddingLeft = padding.left.toFloat()
        paddingRight = padding.right.toFloat()
        paddingTop = padding.top.toFloat()
        paddingBottom = padding.bottom.toFloat()
    }


    private fun createPlayer(player: PlayerPrototype, playerEntity: Entity) {
        playerEntity.run {
            scene2D.run {
                centerX = player.position.x.pixelsToMeters
                centerY = player.position.y.pixelsToMeters
                rotation = player.rotation.toFloat()
            }
            mapObject.run {
                id = player.id
            }
        }
    }

    private fun createFinish(finish: FinishPrototype, finishEntity: Entity) {
        finishEntity.run {
            scene2D.run {
                centerX = finish.position.x.pixelsToMeters
                centerY = finish.position.y.pixelsToMeters
                rotation = finish.rotation.toFloat()
            }
            mapObject.run {
                id = finish.id
            }
        }
    }

    private fun createPlatforms(objects: ArrayList<ObjectPrototype>) {
        objects.forEach {
            when {
                it.type == "platform" -> PlatformEntity.createEntity(
                    it.id,
                    it.position.x.pixelsToMeters,
                    it.position.y.pixelsToMeters,
                    it.width.pixelsToMeters,
                    rotation = it.rotation.toFloat(),
                    isDestroyable = it.isDestroyable
                )
            }
        }
    }

    private fun getMapFileNameForId(
        mapId: Int,
        manager: AssetManager = Injekt.get()
    ): String {
        Gdx.files.local("maps/editor").list().forEach {
            val jsonData = if (manager.isLoaded(it.path())) {
                manager.get<Text>(it.path()).string
            } else {
                Gdx.files.local(it.path()).readString()
            }
            val mapFactory = Json().fromJson(MapFactory::class.java, jsonData)
            if (mapFactory.id == mapId)
                return it.name()
        }
        return ""
    }

    private fun getNewFileName(): String {
        val date = Date(TimeUtils.millis())
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z'.json'", Locale.getDefault())
        return formatter.format(date)
    }

    override fun reset() {
        destroyAllBodies()
        levelId = 1
        mapLeft = Float.POSITIVE_INFINITY
        mapRight = Float.NEGATIVE_INFINITY
        mapBottom = Float.POSITIVE_INFINITY
        mapTop = Float.NEGATIVE_INFINITY
        paddingLeft = 2f
        paddingRight = 2f
        paddingTop = 5f
        paddingBottom = 5f
        updateRoundedPlatforms = true
    }

    fun destroyAllBodies(world: World = Injekt.get()) {
        val bodiesToRemove = Array<Body>()
        world.getBodies(bodiesToRemove)
        bodiesToRemove.forEach {
            world.destroyBody(it)
        }
    }
}

val Entity.map: MapComponent
    get() = MapComponent[this]

fun Entity.map(levelId: Int) =
    add(engine.createComponent(MapComponent::class.java).apply {
        set(levelId)
    })!!