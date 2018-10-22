package ro.luca1152.gravitybox.events

enum class GameEvent {
    // General events
    LEVEL_FINISHED, // The player stayed long enough in the finish point so the level is finished

    // Collisions
    BULLET_PLATFORM_COLLISION, // Collision between a bullet and a wall
    PLAYER_FINISH_COLLISION, // Collision between the player and the finish point
}