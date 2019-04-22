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

package ro.luca1152.gravitybox.entities.game

import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.body
import ro.luca1152.gravitybox.components.game.combinedBody
import ro.luca1152.gravitybox.components.game.platform
import ro.luca1152.gravitybox.components.game.polygon
import ro.luca1152.gravitybox.utils.kotlin.addToEngine
import ro.luca1152.gravitybox.utils.kotlin.newEntity

object CombinedPlatformEntity {
    fun createEntity(
        context: Context,
        isCombinedHorizontally: Boolean = false,
        isCombinedVertically: Boolean = false
    ) = newEntity(context).apply {
        body(context)
        combinedBody(context, this, isCombinedHorizontally, isCombinedVertically, true)
        platform(context)
        editorObject(context)
        polygon(context, false)
        addToEngine(context)
    }
}