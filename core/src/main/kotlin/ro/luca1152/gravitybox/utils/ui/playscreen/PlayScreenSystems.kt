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

package ro.luca1152.gravitybox.utils.ui.playscreen

import com.badlogic.ashley.core.PooledEngine
import ktx.inject.Context
import ro.luca1152.gravitybox.screens.PlayScreen
import ro.luca1152.gravitybox.systems.editor.DashedLineRenderingSystem
import ro.luca1152.gravitybox.systems.editor.SelectedObjectColorSystem
import ro.luca1152.gravitybox.systems.game.*

class PlayScreenSystems(private val context: Context) {
    // Injected objects
    private val engine: PooledEngine = context.inject()
    private val playScreen: PlayScreen = context.inject()

    fun add() {
        engine.run {
            addSystem(EntireLeaderboardCachingSystem(context))
            addSystem(CurrentLevelLeaderboardCachingSystem(context))
            addSystem(WritingLeaderboardToStorageSystem(context))
            addSystem(LeaderboardRankCalculationSystem(context))
            addSystem(FlushPreferencesSystem(context))
            addSystem(PlayTimeSystem(context))
            addSystem(RewardedAdTimerSystem(context))
            addSystem(InterstitialAdsSystem(context))
            addSystem(LevelPlayTimeLoggingSystem(context))
            addSystem(GameFinishSystem(context))
            addSystem(MapLoadingSystem(context))
            addSystem(MapBodiesCreationSystem(context))
            addSystem(CombinedBodiesCreationSystem(context))
            addSystem(RoundedPlatformsSystem(context))
            addSystem(PhysicsSystem(context))
            addSystem(ObjectMovementSystem())
            addSystem(RefilterSystem())
            addSystem(PhysicsSyncSystem())
            addSystem(ShootingSystem(context))
            addSystem(BulletCollisionSystem(context))
            addSystem(PlatformRemovalSystem(context))
            addSystem(OffScreenLevelRestartSystem(context))
            addSystem(OffScreenBulletDeletionSystem(context))
            addSystem(KeyboardLevelRestartSystem(context))
            addSystem(PlayerInsideFinishDetectionSystem())
            addSystem(FinishTimingSystem())
            addSystem(LevelFinishDetectionSystem())
            addSystem(PointsCollectionSystem(context))
            addSystem(LevelRestartSystem(context, playScreen))
            addSystem(CanFinishLevelSystem(context))
            addSystem(FinishPointColorSystem())
            addSystem(ColorSchemeSystem())
            addSystem(SelectedObjectColorSystem())
            addSystem(ColorSyncSystem())
            addSystem(PlayerCameraSystem(context, playScreen))
            addSystem(UpdateGameCameraSystem(context))
            addSystem(DashedLineRenderingSystem(context))
            addSystem(FadeOutFadeInSystem(context))
            addSystem(ImageRenderingSystem(context))
            addSystem(LevelFinishSystem(context))
            addSystem(WriteRankToStorageSystem(context))
            addSystem(ShowNextLevelSystem(context, playScreen))
            addSystem(PromptUserToRateSystem(context, playScreen))
            addSystem(ShowInterstitialAdSystem(context))
            addSystem(ShowFinishStatsSystem(context))
//            addSystem(PhysicsDebugRenderingSystem(context))
            addSystem(DebugRenderingSystem(context))
        }
    }
}