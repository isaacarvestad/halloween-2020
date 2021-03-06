package halloween2020.game

class Game(val map: GameMap) {

    val playerShips = listOf(
            map.shipPos.map { pos -> Ship(pos, 0) },
            map.shipPos.map { pos -> Ship(pos.mirror(), Constants.ANGLE_DEGREES / 2) })
    private val score = intArrayOf(0, 0)
    private var playedRounds = 0
        fun get() = playedRounds

    fun score(player: Int): Int {
        return score[player]
    }

    fun tick(controllers: List<GameController>) {
        updateShips(controllers)
        checkFire()
        updateScores()
        controllers.forEach(GameController::tick)
        playedRounds += 1
    }

    private fun checkFire() {
        playerShips.forEachIndexed { player, ships ->
            ships.forEach { ship ->
                if (ship.alive) {
                    playerShips[1 - player].forEach {
                        it.energy -= ship.firesAt(it)
                    }
                }
            }
        }
        playerShips.forEach { ships ->
            ships.forEach { ship ->
                if (ship.energy < 0) {
                    ship.alive = false
                }
            }
        }
    }

    fun checkFastForward(): Boolean {
        if (playedRounds >= map.rounds) {
            return true
        }
        val p1Ships = playerShips[0].map { ship -> ship.alive }.contains(true)
        val p2Ships = playerShips[1].map { ship -> ship.alive }.contains(true)
        if (p1Ships && p2Ships) {
            return false
        }
        if (!p1Ships && !p2Ships) {
            return true
        }
        // Now exact one player has alive ships
        val bonusPoints = map.influenceCenters.size * (map.rounds - playedRounds)
        if (p1Ships) {
            score[0] += bonusPoints
        } else {
            score[1] += bonusPoints
        }
        return true
    }

    private fun updateScores() {
        val influences = IntArray(map.influenceCenters.size) {0}
        map.influenceCenters.forEachIndexed { index, influenceCenter ->
            playerShips.forEachIndexed { player, ships ->
                ships.forEach {
                    val rad = map.influenceRadius
                    val dist2 = (it.pos - influenceCenter).dist2()
                    if (it.alive && dist2 <= rad * rad) {
                        influences[index] += (if (player == 0) -1 else 1)
                    }
                }
            }
        }
        influences.forEach {
            if (it < 0) {
                score[0]++
            } else if (it > 0) {
                score[1]++
            }
        }
    }

    private fun updateShips(controllers: List<GameController>) {
        playerShips.forEachIndexed { player, ships ->
            ships.forEachIndexed { index, ship ->
                if (ship.alive) {
                    ship.tick(index, controllers[player], this)
                }
            }
        }
    }

}