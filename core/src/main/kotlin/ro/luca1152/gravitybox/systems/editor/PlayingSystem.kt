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
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.components.editor.SelectedObjectComponent
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.listeners.CollisionBoxListener
import ro.luca1152.gravitybox.listeners.WorldContactListener
import ro.luca1152.gravitybox.screens.LevelEditorScreen
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.getSingletonFor
import ro.luca1152.gravitybox.utils.kotlin.removeAndResetEntity
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class PlayingSystem(
    private val levelEditorScreen: LevelEditorScreen,
    private val uiStage: UIStage = Injekt.get(),
    private val manager: AssetManager = Injekt.get(),
    private val world: World = Injekt.get()
) : EntitySystem() {
    private val rootTable = Table().apply {
        setFillParent(true)
        padLeft(62f).padRight(62f)
        padBottom(110f).padTop(110f)
    }
    private var previouslySelectedMapObject: Entity? = null
    private lateinit var skin: Skin
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity

    override fun addedToEngine(engine: Engine) {
        skin = manager.get<Skin>("skins/uiskin.json")
        levelEntity = engine.getSingletonFor(Family.all(LevelComponent::class.java).get()).apply {
            level.forceUpdateMap = true
        }
        playerEntity = engine.getSingletonFor(Family.all(PlayerComponent::class.java).get())
        finishEntity = engine.getSingletonFor(Family.all(FinishComponent::class.java).get())
        setOwnBox2DContactListener()
        makeFinishPointEndlesslyBlink()
        hideLevelEditorUI()
        deselectMapObject()
        removeAllSystems(includingThisSystem = false)
        addPlaySystems()
        showPlayUI()
        updateMapBounds()
    }

    private fun setOwnBox2DContactListener() {
        world.setContactListener(WorldContactListener())
    }

    private fun makeFinishPointEndlesslyBlink() {
        finishEntity.run {
            finish.addPermanentFadeInFadeOutActions(image)
        }
    }

    private fun hideLevelEditorUI() {
        levelEditorScreen.rootTable.isVisible = false
    }

    private fun deselectMapObject() {
        val selectedMapObjects = engine.getEntitiesFor(Family.all(SelectedObjectComponent::class.java).get())
        when {
            selectedMapObjects.size() == 1 -> previouslySelectedMapObject = selectedMapObjects.first()
            else -> previouslySelectedMapObject = null
        }
        previouslySelectedMapObject?.run {
            remove(SelectedObjectComponent::class.java)
        }
    }

    private fun removeAllSystems(includingThisSystem: Boolean) {
        val systemsToRemove = Array<EntitySystem>()
        engine.systems.forEach {
            if (it != this)
                systemsToRemove.add(it)
        }
        systemsToRemove.forEach {
            engine.removeSystem(it)
        }
        if (includingThisSystem)
            engine.removeSystem(this)
    }

    private fun addPlaySystems() {
        engine.run {
            addSystem(MapBodiesCreationSystem())
            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(ShootingSystem())
            addSystem(BulletCollisionSystem())
            addSystem(CollisionBoxListener())
            addSystem(PlatformRemovalSystem())
//            addSystem(PointSystem(mapEntity.map)) TODO
            addSystem(OffScreenLevelRestartSystem())
            addSystem(KeyboardLevelRestartSystem())
            addSystem(LevelFinishDetectionSystem())
            addSystem(LevelFinishSystem(restartLevelWhenFinished = true))
            addSystem(LevelRestartSystem())
            addSystem(FinishPointColorSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem())
            addSystem(UpdateGameCameraSystem())
            addSystem(ImageRenderingSystem())
//            addSystem(PhysicsDebugRenderingSystem())
        }
    }

    private fun showPlayUI() {
        rootTable.add(createBackButton()).expand().bottom().left()
        uiStage.addActor(rootTable)
    }

    private fun updateMapBounds() {
        levelEntity.map.updateMapBounds()
    }

    private fun createBackButton() = ClickButton(skin, "small-button").apply {
        addIcon("back-icon")
        iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            removeAllSystems(true)
        })
    }

    override fun removedFromEngine(engine: Engine) {
        Colors.resetAllColors()
        hidePlayUI()
        showLevelEditorUI()
        enableMoveTool()
        removePlayEntities(engine)
        resetEntitiesPosition(engine)
        removeFinishPointEndlessBlink()
        reselectMapObject(engine)
        levelEditorScreen.addGameSystems()
    }

    private fun hidePlayUI() {
        rootTable.remove()
    }

    private fun enableMoveTool() {
        levelEditorScreen.moveToolButton.isToggled = true
    }

    private fun removePlayEntities(engine: Engine) {
        removeEveryBullet(engine)
    }

    private fun resetEntitiesPosition(engine: Engine) {
        engine.getEntitiesFor(Family.all(ImageComponent::class.java, BodyComponent::class.java).get()).forEach {
            it.image.run {
                this.centerX = it.body.initialX
                this.centerY = it.body.initialY
                this.img.rotation = it.body.initialRotationRad * MathUtils.radiansToDegrees
            }
        }
    }

    private fun removeFinishPointEndlessBlink() {
        finishEntity.image.img.run {
            clearActions()
            color.a = 1f
        }
    }

    private fun reselectMapObject(engine: Engine) {
        previouslySelectedMapObject?.run {
            add(engine.createComponent(SelectedObjectComponent::class.java))
        }
    }

    private fun removeEveryBullet(engine: Engine) {
        val entitiesToRemove = Array<Entity>()
        engine.getEntitiesFor(Family.all(BulletComponent::class.java).get()).forEach {
            entitiesToRemove.add(it)
        }
        entitiesToRemove.forEach {
            engine.removeAndResetEntity(it)
        }
    }

    private fun showLevelEditorUI() {
        levelEditorScreen.rootTable.isVisible = true
    }
}