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

package ro.luca1152.gravitybox.utils.ui.button

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Pools

/** A button which triggers the appearance of a pane if touched while toggled. */
class PaneButton(skin: Skin, styleName: String) : ToggleButton(skin, styleName) {
    private val paneTable = Table(skin)
    private var cellCount = 0
    private var touchLevel = 0
    private var paneWidthInCells = 3
    fun addCellToPane(cell: Table) {
        cellCount++
        paneTable.add(cell)
        if (cellCount % paneWidthInCells == 0) {
            paneTable.row()
        }
    }

    init {
        name = "PaneButton"
        paneTable.defaults().padLeft(-1f).padBottom(-1f)
        toggleOnce = false
        toggleRunnable = Runnable {
            touchLevel = if (touchLevel + 1 > 2) 1 else (touchLevel + 1)
            when (touchLevel) {
                1 -> hidePane()
                2 -> showPane()
            }
        }
    }

    private fun showPane() {
        stage.addActor(paneTable)
        val buttonTopRight = this.localToStageCoordinates(Pools.obtain(Vector2::class.java).set(width, height))
        paneTable.setPosition(buttonTopRight.x + paneTable.prefWidth / 2f, buttonTopRight.y - paneTable.prefHeight / 2f)
    }

    private fun hidePane() {
        paneTable.remove()
    }

    fun clickedOutsidePane() {
        hidePane()
        touchLevel = 0
    }

    fun clickedOnButtonFromPane() {
        hidePane()
        touchLevel = 1
    }
}