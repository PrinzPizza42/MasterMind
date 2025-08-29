data class GameResult(
    val won: Boolean,
    val tries: Int,
    val columnSize: Int,
    val columnCount: Int,
    val colorAmount: Int
) {
    //val player: Player
}
