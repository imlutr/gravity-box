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

package ro.luca1152.gravitybox.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.map.Map
import ro.luca1152.gravitybox.utils.map.MapObject
import ro.luca1152.gravitybox.utils.map.objects.PlatformObject
import ro.luca1152.gravitybox.utils.ui.ColorScheme
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class MyMapRenderingSystem(private val map: Map,
                           private val stage: GameStage = Injekt.get(),
                           private val manager: AssetManager = Injekt.get()) : EntitySystem() {
    private val mapGroup = Group()
    private val addedObjects = Array<MapObject>()

    override fun addedToEngine(engine: Engine?) {
        stage.addActor(mapGroup)
    }

    override fun update(deltaTime: Float) {
        // Search for objects that were not added to the [mapGroup] and add them
        for (i in 0 until map.objects.size) {
            val it = map.objects[i]
            if (!addedObjects.contains(it, true)) {
                val actor = when (it) {
                    is PlatformObject -> createPlatform(it)
                    else -> TODO()
                }
                mapGroup.addActor(actor)
            }
        }
    }

    private fun createPlatform(platformObject: PlatformObject) = Image(manager.get<Texture>("graphics/pixel.png")).apply {
        setPosition(platformObject.x, platformObject.y)
        setSize(platformObject.width, platformObject.height)
        color = ColorScheme.currentDarkColor
    }

    override fun removedFromEngine(engine: Engine?) {
        mapGroup.remove()
    }
}