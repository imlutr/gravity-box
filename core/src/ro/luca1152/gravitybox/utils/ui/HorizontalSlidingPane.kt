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

package ro.luca1152.gravitybox.utils.ui

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener

class HorizontalSlidingPane(private val pageWidth: Float, private val pageHeight: Float,
                            private val flingSpeedThreshold: Float = 600f, // The speed needed to fling to the next page
                            private val overScrollDistance: Float = 100f  // E.g. how much to the left can you scroll on the first page
) : Group() {
    // The pages container. Contains each page.
    private val pages: Group = Group()

    // The listener that automatically handles flings, pans, taps.
    private val inputListener: ActorGestureListener

    // The number of pages
    val pagesCount
        get() = pages.children.size

    // The current page. It modifies when the player pans. It is float, so it can be compared with the target page.
    // If it was int, and the player panned to page 1.5, it would get rounded to 2, and the check [currentPage != targetPage] from scrollToTargetPage() would fail
    val currentPage
        get() = Math.abs(pages.x / pageWidth) + 1

    // The page to which it will be automatically scrolled
    private var targetPage = 1

    // The x to which it will be automatically scrolled
    // It is -[...] because the x has values in [-((targetPage - 1) * pageWidth), 0] and not [0, (targetPage - 1) * pageWidth]
    private val targetX
        get() = -((targetPage - 1) * pageWidth)

    val isPanning
        get() = inputListener.gestureDetector.isPanning

    init {
        this.addActor(pages)

        inputListener = object : ActorGestureListener() {
            override fun pan(event: InputEvent?, x: Float, y: Float, deltaX: Float, deltaY: Float) {
                pages.run {
                    moveBy(deltaX, 0f)

                    // Don't over-pan
                    this.x = MathUtils.clamp(pages.x, -((pages.children.size - 1) * pageWidth + overScrollDistance), overScrollDistance)
                }
            }

            override fun fling(event: InputEvent?, velocityX: Float, velocityY: Float, button: Int) {
                if (Math.abs(velocityX) > flingSpeedThreshold) {
                    targetPage = when {
                        velocityX > 0 -> MathUtils.clamp(currentPage.toInt(), 1, pagesCount) // The fling was to the left
                        else -> MathUtils.clamp(currentPage.toInt() + 1, 1, pagesCount) // The fling was to the right
                    }
                }
            }
        }
        addListener(inputListener)
    }

    fun addPage(page: Actor) {
        pages.addActor(page.apply {
            // If it's not touchable, the pane slides only if you pan on the buttons, and not on the whole area.
            touchable = Touchable.enabled

            setPosition(pagesCount * pageWidth - pageWidth / 2f, -pageHeight / 2f)
            setSize(pageWidth, pageHeight)
        })
    }

    /** Called every frame, equivalent to update() */
    override fun act(delta: Float) {
        pages.act(delta)

        if (isPanning) targetPage = Math.round(currentPage)
        else scrollToTargetPage()
    }

    private fun scrollToTargetPage() {
        if (currentPage != targetPage.toFloat() && pages.actions.count() == 0) {
            pages.addAction(moveTo(targetX, 0f, .125f))
        }
    }
}