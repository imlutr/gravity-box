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
import ktx.inject.Context
import ro.luca1152.gravitybox.components.editor.*
import ro.luca1152.gravitybox.components.game.*
import ro.luca1152.gravitybox.entities.game.FinishEntity
import ro.luca1152.gravitybox.screens.LevelEditorScreen
import ro.luca1152.gravitybox.systems.game.*
import ro.luca1152.gravitybox.utils.assets.Assets
import ro.luca1152.gravitybox.utils.box2d.WorldContactListener
import ro.luca1152.gravitybox.utils.kotlin.UIStage
import ro.luca1152.gravitybox.utils.kotlin.filterNullableSingleton
import ro.luca1152.gravitybox.utils.kotlin.getSingleton
import ro.luca1152.gravitybox.utils.kotlin.tryGet
import ro.luca1152.gravitybox.utils.ui.Colors
import ro.luca1152.gravitybox.utils.ui.button.ClickButton

/** Makes edited levels playable. */
class PlayingSystem(
    private val context: Context,
    private val levelEditorScreen: LevelEditorScreen
) : EntitySystem() {
    private val manager: AssetManager = context.inject()
    private val uiStage: UIStage = context.inject()
    private val world: World = context.inject()
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
        makePointsEndlesslyBlink()
        hideRotatingIndicators()
        hideLevelEditorUI()
        hideMockObjects()
        deselectMapObject()
        removeAllSystems(includingThisSystem = false)
        addPlaySystems()
        showPlayUI()
        updateMapBounds()
        makeEveryObjectOpaque()
        resetCollectedPointsCount()
    }

    private fun resetCollectedPointsCount() {
        levelEntity.map.collectedPointsCount = 0
    }

    private fun setOwnBox2DContactListener() {
        world.setContactListener(WorldContactListener(context))
    }

    private fun makeFinishPointEndlesslyBlink() {
        if (levelEntity.level.canFinish) {
            finishEntity.fadeInFadeOut(context, finishEntity.scene2D)
        } else {
            finishEntity.scene2D.color.a = FinishEntity.FINISH_BLOCKED_ALPHA
        }
    }

    private fun makePointsEndlesslyBlink() {
        engine.getEntitiesFor(Family.all(CollectiblePointComponent::class.java).get()).forEach {
            if (it.tryGet(FadeInFadeOutComponent) == null) {
                it.fadeInFadeOut(context, it.scene2D)
            }
        }
    }

    private fun hideRotatingIndicators() {
        engine.getEntitiesFor(Family.all(RotatingIndicatorComponent::class.java).get()).forEach {
            it.rotatingIndicator.indicatorImage.isVisible = false
        }
    }

    private fun hideLevelEditorUI() {
        levelEditorScreen.rootTable.isVisible = false
    }

    private fun hideMockObjects() {
        engine.getEntitiesFor(Family.all(MockMapObjectComponent::class.java).get()).forEach {
            it.scene2D.run {
                isVisible = false
                isTouchable = false
            }
        }
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
            addSystem(MapBodiesCreationSystem(context))
            addSystem(CombinedBodiesCreationSystem(context))
            addSystem(PhysicsSystem(context))
            addSystem(ObjectMovementSystem())
            addSystem(RefilterSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(LevelRestartSystem(context))
            addSystem(ShootingSystem(context))
            addSystem(BulletCollisionSystem(context))
            addSystem(PlatformRemovalSystem(context))
            addSystem(OffScreenLevelRestartSystem(context))
            addSystem(OffScreenBulletDeletionSystem(context))
            addSystem(KeyboardLevelRestartSystem(context))
            addSystem(LevelFinishDetectionSystem())
            addSystem(PointsCollectionSystem(context))
            addSystem(LevelFinishSystem(context, restartLevelWhenFinished = true))
            addSystem(FinishPointColorSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(RoundedPlatformsSystem(context))
            addSystem(ColorSyncSystem())
            addSystem(CanFinishLevelSystem(context))
            addSystem(PlayerCameraSystem(context))
            addSystem(UpdateGameCameraSystem(context))
            addSystem(FadeOutFadeInSystem(context))
            addSystem(ImageRenderingSystem(context))
//            addSystem(PhysicsDebugRenderingSystem(context))
            addSystem(DebugRenderingSystem(context))
        }
    }

    private fun showPlayUI() {
        uiStage.addActor(rootTable)
    }

    private fun updateMapBounds() {
        levelEntity.map.updateMapBounds()
    }

    private fun makeEveryObjectOpaque() {
        engine.getEntitiesFor(Family.all(EditorObjectComponent::class.java).get()).forEach {
            if (it.tryGet(Scene2DComponent) != null) {// Crashes if I don't check..
                it.scene2D.color.a = 1f
            }
        }
    }

    override fun removedFromEngine(engine: Engine) {
        Colors.resetAllColors()
        hidePlayUI()
        showLevelEditorUI()
        showMockObjects(engine)
        enableMoveTool()
        removePlayEntities(engine)
        resetEntitiesPosition(engine)
        removeFinishPointEndlessBlink()
        removePointsEndlessBlink(engine)
        showRotatingIndicators(engine)
        reselectMapObject()
        destroyAllBodies()
        resetDestroyablePlatforms(engine)
        resetCollectiblePoints(engine)
        levelEditorScreen.addGameSystems()
        resetCollectedPointsCount()
        levelEntity.map.resetPassengers()
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

    private fun removePointsEndlessBlink(engine: Engine) {
        engine.getEntitiesFor(Family.all(CollectiblePointComponent::class.java).get()).forEach {
            if (it.tryGet(FadeInFadeOutComponent) != null) {
                it.remove(FadeInFadeOutComponent::class.java)
            }
        }
    }

    private fun showRotatingIndicators(engine: Engine) {
        engine.getEntitiesFor(Family.all(RotatingIndicatorComponent::class.java).get()).forEach {
            it.rotatingIndicator.indicatorImage.isVisible = true
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
            engine.removeEntity(it)
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
                it.body(context)
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

    private fun showMockObjects(engine: Engine) {
        engine.getEntitiesFor(Family.all(MockMapObjectComponent::class.java).get()).forEach {
            if (it.tryGet(EditorObjectComponent) == null || !it.editorObject.isDeleted) {
                it.scene2D.run {
                    isVisible = true
                    isTouchable = true
                }
            }
        }
    }
}