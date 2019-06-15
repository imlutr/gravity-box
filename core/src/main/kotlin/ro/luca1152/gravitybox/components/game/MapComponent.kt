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
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter
import com.badlogic.gdx.utils.Pool.Poolable
import com.badlogic.gdx.utils.TimeUtils
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.entities.editor.MovingMockPlatformEntity
import ro.luca1152.gravitybox.entities.game.CollectiblePointEntity
import ro.luca1152.gravitybox.entities.game.DashedLineEntity
import ro.luca1152.gravitybox.entities.game.PlatformEntity
import ro.luca1152.gravitybox.entities.game.TextEntity
import ro.luca1152.gravitybox.events.EventQueue
import ro.luca1152.gravitybox.events.FadeInEvent
import ro.luca1152.gravitybox.events.FadeOutFadeInEvent
import ro.luca1152.gravitybox.events.UpdateRoundedPlatformsEvent
import ro.luca1152.gravitybox.utils.assets.json.*
import ro.luca1152.gravitybox.utils.assets.loaders.Text
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.removeComponent
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import ro.luca1152.gravitybox.utils.ui.Colors
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
    // Injected objects
    private lateinit var engine: PooledEngine
    private lateinit var manager: AssetManager
    private lateinit var world: World
    private lateinit var eventQueue: EventQueue

    var levelId = 1
    var hue = 180

    var pointsCount = 0
    var collectedPointsCount = 0

    var mapLeft = Float.POSITIVE_INFINITY
    var mapRight = Float.NEGATIVE_INFINITY
    var mapBottom = Float.POSITIVE_INFINITY
    var mapTop = Float.NEGATIVE_INFINITY

    var forceCenterCameraOnPlayer = false

    var paddingLeft = 2f
    var paddingRight = 2f
    var paddingTop = 5f
    var paddingBottom = 5f

    fun set(context: Context, levelId: Int, hue: Int) {
        this.levelId = levelId
        this.hue = hue
        injectObjects(context)
    }

    private fun injectObjects(context: Context) {
        engine = context.inject()
        manager = context.inject()
        world = context.inject()
        eventQueue = context.inject()
    }

    fun updateMapBounds() {
        mapLeft = Float.POSITIVE_INFINITY
        mapRight = Float.NEGATIVE_INFINITY
        mapBottom = Float.POSITIVE_INFINITY
        mapTop = Float.NEGATIVE_INFINITY
        engine.getSingleton<PlayerComponent>().polygon.update()
        engine.getSingleton<FinishComponent>().polygon.update()
        engine.getEntitiesFor(Family.all(PolygonComponent::class.java).get()).forEach {
            if ((it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) && !it.isScheduledForRemoval) {
                it.polygon.run {
                    mapLeft = if (leftmostX != Float.NEGATIVE_INFINITY) Math.min(mapLeft, leftmostX) else mapLeft
                    mapRight = if (rightmostX != Float.POSITIVE_INFINITY) Math.max(mapRight, rightmostX) else mapRight
                    mapBottom = if (bottommostY != Float.NEGATIVE_INFINITY) Math.min(mapBottom, bottommostY) else mapBottom
                    mapTop = if (topmostY != Float.POSITIVE_INFINITY) Math.max(mapTop, topmostY) else mapTop
                }
            }
        }
    }

    /** @param forceSave If true, it will override any previous map, even if the new file's content is the same. */
    fun saveMap(forceSave: Boolean = false) {
        val json = getJsonFromMap()
        writeJsonToFile(json, forceSave)
    }

    private fun getJsonFromMap(): Json {
        var player: Entity? = null
        var finishPoint: Entity? = null
        val objects = Array<Entity>()
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
                    it.tryGet(PlatformComponent) != null || it.tryGet(DestroyablePlatformComponent) != null ||
                            it.tryGet(CollectiblePointComponent) != null || it.tryGet(TextComponent) != null -> {
                        objects.add(it)
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
            writeValue("hue", hue)
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
            objects.forEach {
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
        context: Context,
        mapFactory: MapFactory,
        playerEntity: Entity,
        finishEntity: Entity,
        isLevelEditor: Boolean = false
    ) {
        resetPoints()
        resetFinish(context)
        removeObjects()
        resetPassengers()
        createMap(mapFactory.id, mapFactory.hue, mapFactory.padding)
        createPlayer(mapFactory.player, playerEntity)
        createFinish(mapFactory.finish, finishEntity)
        createObjects(context, mapFactory.objects, isLevelEditor)
        updateMapBounds()
        eventQueue.run {
            clear()
            add(UpdateRoundedPlatformsEvent())

            // Clear all actions in case the level was restarting, causing visual glitches
            add(FadeInEvent(FadeOutFadeInEvent.CLEAR_ACTIONS))
        }
    }

    fun resetPassengers() {
        val entitiesToReset = ArrayList<Entity>()
        engine.getEntitiesFor(Family.all(PassengerComponent::class.java).get()).forEach {
            entitiesToReset.add(it)
        }
        entitiesToReset.forEach {
            it.removeComponent<PassengerComponent>()
        }
    }

    private fun resetPoints() {
        collectedPointsCount = 0
        pointsCount = 0
        engine.getSingleton<LevelComponent>().level.canFinish = true
    }

    private fun resetFinish(context: Context) {
        val finishEntity = engine.getSingleton<FinishComponent>()
        if (finishEntity.tryGet(FadeInFadeOutComponent) == null) {
            finishEntity.fadeInFadeOut(context, finishEntity.scene2D)
        }
    }

    private fun removeObjects() {
        val entitiesToRemove = Array<Entity>()
        engine.getEntitiesFor(
            Family.exclude(
                PlayerComponent::class.java,
                FinishComponent::class.java,
                LevelComponent::class.java,
                UndoRedoComponent::class.java,
                InputComponent::class.java
            ).get()
        ).forEach {
            entitiesToRemove.add(it)
        }
        entitiesToRemove.forEach {
            engine.removeEntity(it)
        }
    }

    private fun createMap(id: Int, hue: Int, padding: PaddingPrototype) {
        levelId = id

        this.hue = hue
        Colors.hue = hue
        Colors.LightTheme.resetAllColors(Colors.hue)
        Colors.DarkTheme.resetAllColors(Colors.hue)

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
        }
    }

    private fun createFinish(finish: FinishPrototype, finishEntity: Entity) {
        finishEntity.run {
            scene2D.run {
                centerX = finish.position.x.pixelsToMeters
                centerY = finish.position.y.pixelsToMeters
                rotation = finish.rotation.toFloat()
            }
        }
    }

    private fun createObjects(
        context: Context,
        objects: ArrayList<ObjectPrototype>,
        isLevelEditor: Boolean
    ) {
        objects.forEach {
            when {
                it.type == "platform" -> createPlatform(context, it, isLevelEditor)
                it.type == "point" -> createPoint(context, it)
                it.type == "text" -> createText(context, it)
                it.type == "dashed-line" -> createDashedLine(context, it)
            }
        }
    }

    private fun createPlatform(context: Context, platform: ObjectPrototype, isLevelEditor: Boolean) {
        val newPlatform = PlatformEntity.createEntity(
            context,
            platform.position.x.pixelsToMeters,
            platform.position.y.pixelsToMeters,
            platform.width.pixelsToMeters,
            rotation = platform.rotation.toFloat(),
            isDestroyable = platform.isDestroyable,
            isRotating = platform.isRotating,
            targetX = platform.movingTo.x.pixelsToMeters,
            targetY = platform.movingTo.y.pixelsToMeters,
            speed = platform.speed.pixelsToMeters
        )
        if (platform.movingTo.x != Float.POSITIVE_INFINITY && platform.movingTo.y != Float.POSITIVE_INFINITY) {
            val mockPlatform = MovingMockPlatformEntity.createEntity(
                context, newPlatform,
                platform.movingTo.x.pixelsToMeters, platform.movingTo.y.pixelsToMeters,
                newPlatform.scene2D.width, newPlatform.scene2D.rotation
            )
            newPlatform.linkedEntity(context, "mockPlatform", mockPlatform)

            if (isLevelEditor) {
                val dashedLine = DashedLineEntity.createEntity(context, newPlatform, mockPlatform)
                mockPlatform.linkedEntity.add("dashedLine", dashedLine)
                newPlatform.linkedEntity.add("dashedLine", dashedLine)
            } else {
                // Even if it is not in the level editor, the mock platform should still be added as it
                // is used to determine map bounds as the target position should also be used in calculations
                mockPlatform.scene2D.isVisible = false
            }
        }
        if (isLevelEditor && platform.isRotating) {
            newPlatform.rotatingIndicator(context)
        }
    }

    private fun createPoint(context: Context, point: ObjectPrototype) {
        pointsCount++
        CollectiblePointEntity.createEntity(
            context,
            point.position.x.pixelsToMeters,
            point.position.y.pixelsToMeters,
            point.rotation.toFloat()
        )
    }

    private fun createText(context: Context, text: ObjectPrototype) {
        TextEntity.createEntity(
            context,
            text.string,
            text.position.x,
            text.position.y
        )
    }

    private fun createDashedLine(context: Context, dashedLine: ObjectPrototype) {
        DashedLineEntity.createEntity(
            context,
            dashedLine.start.x.pixelsToMeters, dashedLine.start.y.pixelsToMeters,
            dashedLine.end.x.pixelsToMeters, dashedLine.end.y.pixelsToMeters
        )
    }

    private fun getMapFileNameForId(mapId: Int): String {
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
        val formatter = SimpleDateFormat("yyyy-MM-dd HHmmss z'.json'", Locale.getDefault())
        return formatter.format(date)
    }

    override fun reset() {
        destroyAllBodies()
        levelId = 1
        hue = 180
        pointsCount = 0
        collectedPointsCount = 0
        mapLeft = Float.POSITIVE_INFINITY
        mapRight = Float.NEGATIVE_INFINITY
        mapBottom = Float.POSITIVE_INFINITY
        mapTop = Float.NEGATIVE_INFINITY
        paddingLeft = 2f
        paddingRight = 2f
        paddingTop = 5f
        paddingBottom = 5f
        forceCenterCameraOnPlayer = false
    }

    fun destroyAllBodies() {
        val bodiesToDestroy = ArrayList<Entity>()
        engine.getEntitiesFor(Family.all(BodyComponent::class.java).get()).forEach {
            bodiesToDestroy.add(it)
        }
        bodiesToDestroy.forEach {
            it.body.destroyBody()
        }
    }

    companion object : ComponentResolver<MapComponent>(MapComponent::class.java)
}

val Entity.map: MapComponent
    get() = MapComponent[this]

fun Entity.map(context: Context, levelId: Int, hue: Int) =
    add(createComponent<MapComponent>(context).apply {
        set(context, levelId, hue)
    })!!