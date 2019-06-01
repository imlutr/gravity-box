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

package ro.luca1152.gravitybox.systems.editor

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.TimeUtils
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.MapObjectComponent
import ro.luca1152.gravitybox.utils.kotlin.GameStage
import ro.luca1152.gravitybox.utils.kotlin.hitAllScreen
import ro.luca1152.gravitybox.utils.kotlin.tryGet

/** Allows tapping through objects at long press. */
class TapThroughObjectsSystem(context: Context) : EntitySystem() {
    private val inputMultiplexer: InputMultiplexer = context.inject()
    private val gameStage: GameStage = context.inject()

    private val gestureListener = object : GestureDetector(GestureAdapter()) {
        private var gestureStartTime = 0L

        override fun touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean {
            gestureStartTime = TimeUtils.nanoTime()
            return super.touchDown(x, y, pointer, button)
        }

        override fun touchUp(x: Int, y: Int, pointer: Int, button: Int): Boolean {
            if (TimeUtils.nanoTime() - gestureStartTime > 0.75f * (1e9 + 1)) {
                val sortedHitActors = gameStage.hitAllScreen(x, y).filter { isMapObject(it) }.sortedBy { it.zIndex }
                    .distinctBy { (it.userObject as Entity) }
                if (sortedHitActors.isEmpty()) {
                    return false
                }
                var selectedObjectZIndex = sortedHitActors.firstOrNull { it.isSelected }?.zIndex ?: -1
                if (selectedObjectZIndex == sortedHitActors.last().zIndex) selectedObjectZIndex = -1
                selectFirstObjectWithZIndexHigherThan(sortedHitActors, selectedObjectZIndex)
                return true
            }
            return false
        }

        private fun selectFirstObjectWithZIndexHigherThan(sortedHitActors: List<Actor>, zIndex: Int) {
            deselectAllObjects()
            (sortedHitActors.first { it.zIndex > zIndex }.userObject as Entity).editorObject.isSelected = true
        }

        private fun deselectAllObjects() {
            engine.getEntitiesFor(Family.all(EditorObjectComponent::class.java).get()).forEach {
                it.editorObject.isSelected = false
            }
        }

        private fun isMapObject(actor: Actor?): Boolean {
            return if (actor == null || actor.userObject == null || actor.userObject !is Entity) false
            else (actor.userObject as Entity).tryGet(MapObjectComponent) != null && !isDeleted(actor.userObject as Entity)
        }

        private fun isDeleted(entity: Entity) =
            entity.tryGet(EditorObjectComponent) != null && entity.editorObject.isDeleted

        private val Actor.isSelected: Boolean
            get() = (userObject as Entity).editorObject.isSelected
    }

    override fun addedToEngine(engine: Engine?) {
        inputMultiplexer.addProcessor(gestureListener)
    }

    override fun removedFromEngine(engine: Engine?) {
        inputMultiplexer.removeProcessor(gestureListener)
    }
}