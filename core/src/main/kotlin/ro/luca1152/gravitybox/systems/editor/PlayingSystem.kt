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
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import ro.luca1152.gravitybox.components.editor.EditorObjectComponent
import ro.luca1152.gravitybox.components.editor.editorObject
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.screens.LevelEditorScreen
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.WorldContactListener
import ro.luca1152.gravitybox.utils.kotlin.*
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.button.ClickButton
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

/** Makes edited levels playable. */
class PlayingSystem(
    private val levelEditorScreen: LevelEditorScreen,
    manager: AssetManager = Injekt.get(),
    private val uiStage: UIStage = Injekt.get(),
    private val world: World = Injekt.get()
) : EntitySystem() {
    private val skin = manager.get(Assets.uiSkin)
    private val backButton = ClickButton(skin, "small-button").apply {
        addIcon("back-icon")
        iconCell!!.padLeft(-5f) // The back icon doesn't LOOK centered (even though it is)
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            removeAllSystems(true)
        })
    }

    private val restartButton = ClickButton(skin, "small-button").apply {
        addIcon("redo-icon")
        setColors(Colors.gameColor, Colors.uiDownColor)
        addClickRunnable(Runnable {
            levelEntity.level.restartLevel = true
        })
    }

    private val bottomRow = Table().apply {
        add(backButton).expand().left()
        add(restartButton).expand().right()
    }

    private val rootTable = Table().apply {
        setFillParent(true)
        padLeft(62f).padRight(62f)
        padBottom(110f).padTop(110f)
        add(bottomRow).expand().fillX().bottom()
    }

    private var previouslySelectedMapObject: Entity? = null
    private lateinit var levelEntity: Entity
    private lateinit var playerEntity: Entity
    private lateinit var finishEntity: Entity

    override fun addedToEngine(engine: Engine) {
        levelEntity = engine.getSingleton<LevelComponent>().apply {
            level.forceUpdateMap = true
        }
        playerEntity = engine.getSingleton<PlayerComponent>()
        finishEntity = engine.getSingleton<FinishComponent>()
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
        finishEntity.fadeInFadeOut(finishEntity.scene2D)
    }

    private fun hideLevelEditorUI() {
        levelEditorScreen.rootTable.isVisible = false
    }

    private fun deselectMapObject() {
        engine.getEntitiesFor(Family.all(EditorObjectComponent::class.java).get())
            .filterNullableSingleton { it.editorObject.isSelected }?.let {
                it.editorObject.isSelected = false
                previouslySelectedMapObject = it
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
            addSystem(CombinedBodiesCreationSystem())
            addSystem(PhysicsSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(ShootingSystem())
            addSystem(BulletCollisionSystem())
            addSystem(PlatformRemovalSystem())
            addSystem(OffScreenLevelRestartSystem())
            addSystem(OffScreenBulletDeletionSystem())
            addSystem(KeyboardLevelRestartSystem())
            addSystem(LevelFinishDetectionSystem())
            addSystem(PointsCollectionSystem())
            addSystem(LevelFinishSystem(restartLevelWhenFinished = true))
            addSystem(LevelRestartSystem())
            addSystem(FinishPointColorSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(RoundedPlatformsSystem())
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem())
            addSystem(UpdateGameCameraSystem())
            addSystem(ImageRenderingSystem())
//            addSystem(PhysicsDebugRenderingSystem())
        }
    }

    private fun showPlayUI() {
        uiStage.addActor(rootTable)
    }

    private fun updateMapBounds() {
        levelEntity.map.updateMapBounds()
    }

    override fun removedFromEngine(engine: Engine) {
        Colors.resetAllColors()
        hidePlayUI()
        showLevelEditorUI()
        enableMoveTool()
        removePlayEntities(engine)
        resetEntitiesPosition(engine)
        removeFinishPointEndlessBlink()
        reselectMapObject()
        destroyAllBodies()
        resetDestroyablePlatforms(engine)
        resetCollectiblePoints(engine)
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
        engine.getEntitiesFor(
            Family.all(Scene2DComponent::class.java, BodyComponent::class.java)
                .exclude(CombinedBodyComponent::class.java).get()
        ).forEach {
            it.scene2D.run {
                centerX = it.body.initialX
                centerY = it.body.initialY
                rotation = it.body.initialRotationRad * MathUtils.radiansToDegrees
            }
        }
    }

    private fun removeFinishPointEndlessBlink() {
        finishEntity.scene2D.group.run {
            clearActions()
            color.a = 1f
        }
    }

    private fun reselectMapObject() {
        previouslySelectedMapObject?.editorObject?.isSelected = true
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

    private fun destroyAllBodies() {
        levelEntity.map.destroyAllBodies()
    }

    private fun resetDestroyablePlatforms(engine: Engine) {
        engine.getEntitiesFor(Family.all(DestroyablePlatformComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                it.scene2D.isVisible = true
                it.destroyablePlatform.isRemoved = false
                it.body()
            }
        }
    }

    private fun resetCollectiblePoints(engine: Engine) {
        engine.getEntitiesFor(Family.all(CollectiblePointComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                it.scene2D.isVisible = true
                it.collectiblePoint.isCollected = false
            }
        }
    }

    private fun showLevelEditorUI() {
        levelEditorScreen.rootTable.isVisible = true
    }
}