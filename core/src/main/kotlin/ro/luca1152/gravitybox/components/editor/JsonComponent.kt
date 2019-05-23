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

package ro.luca1152.gravitybox.components.editor

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.inject.Context
import ro.luca1152.gravitybox.components.ComponentResolver
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.utils.kotlin.createComponent
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Translates the entity's information to JSON. */
class JsonComponent : Component, Poolable {
    private var parentEntity: Entity? = null
    private var jsonObjectName = ""
    private var jsonWillBeInArray = false

    fun setObject(parentEntity: Entity, jsonObjectName: String = "") {
        this.parentEntity = parentEntity
        this.jsonObjectName = jsonObjectName
    }

    fun setArrayObject(parentEntity: Entity) {
        jsonWillBeInArray = true
        setObject(parentEntity, "")
    }

    fun writeToJson(json: Json) {
        if (jsonWillBeInArray) {
            json.writeObjectStart()
        } else {
            json.writeObjectStart(jsonObjectName)
        }
        parentEntity.run {
            this?.let {
                when {
                    tryGet(PlatformComponent) != null || tryGet(DestroyablePlatformComponent) != null -> {
                        json.writeValue("type", "platform")
                    }
                    tryGet(CollectiblePointComponent) != null -> {
                        json.writeValue("type", "point")
                    }
                    tryGet(TextComponent) != null -> {
                        json.writeValue("type", "text")
                    }
                }
                if (tryGet(Scene2DComponent) != null) json.run {
                    if (tryGet(TextComponent) != null) json.run {
                        writeValue("string", text.string)
                    }
                    writeObjectStart("position")
                    writeValue("x", scene2D.centerX.metersToPixels)
                    writeValue("y", scene2D.centerY.metersToPixels)
                    writeObjectEnd()
                    if (tryGet(MovingObjectComponent) != null) json.run {
                        writeObjectStart("movingTo")
                        writeValue("x", movingObject.endPoint.x.metersToPixels)
                        writeValue("y", movingObject.endPoint.y.metersToPixels)
                        writeObjectEnd()
                        writeValue("speed", movingObject.speed.metersToPixels)
                    }
                    if (tryGet(PlatformComponent) != null || tryGet(DestroyablePlatformComponent) != null) json.run {
                        writeValue("width", scene2D.width.metersToPixels)
                    }
                    if (scene2D.rotation != 0f) {
                        writeValue("rotation", scene2D.rotation.toInt())
                    }
                    if (tryGet(DestroyablePlatformComponent) != null) json.run {
                        writeValue("isDestroyable", true)
                    }
                    if (tryGet(RotatingObjectComponent) != null) json.run {
                        writeValue("isRotating", true)
                    }
                }
            }
        }
        json.writeObjectEnd()
    }

    override fun reset() {
        jsonObjectName = ""
        jsonWillBeInArray = false
        parentEntity = null
    }

    companion object : ComponentResolver<JsonComponent>(JsonComponent::class.java)
}

val Entity.json: JsonComponent
    get() = JsonComponent[this]

fun Entity.json(context: Context) =
    add(createComponent<JsonComponent>(context))!!

fun Entity.json(context: Context, parentEntity: Entity, jsonObjectName: String) =
    add(createComponent<JsonComponent>(context).apply {
        setObject(parentEntity, jsonObjectName)
    })!!

fun Entity.json(context: Context, parentEntity: Entity) =
    add(createComponent<JsonComponent>(context).apply {
        setArrayObject(parentEntity)
    })!!