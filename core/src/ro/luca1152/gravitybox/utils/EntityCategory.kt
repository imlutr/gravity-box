package ro.luca1152.gravitybox.utils

enum class EntityCategory(bits: Int) {
    NONE(0x0000),
    FINISH(0x0001),
    PLAYER(0x0002),
    OBSTACLE(0x0003),
    BULLET(0x0004);

    var bits: Short = 0

    init {
        this.bits = bits.toShort()
    }
}