import GameManager.Companion.Ships
import GameManager.Companion.Ships.CONTRATORPEDEIROS
import GameManager.Companion.Ships.SUBMARINOS
import GameManager.Companion.Ships.PORTA_AVIAO
import GameManager.Companion.Ships.NAVIO_TANQUE
import GameManager.Companion.Point
import GameManager.Companion.Orientations
import java.net.ServerSocket

fun main(args: Array<String>) {
    Server().createServerShips().startGame()
}

class Server {

    private val PORT = 6789
    private val serverSocket = ServerSocket(PORT)
    private val gameManager = GameManager(serverSocket.accept())

    companion object {
        @JvmStatic
        fun getRandomPoint(): Point {
            return Point((Math.random() * 9).toInt(),(Math.random() * 9).toInt())
        }
    }

    fun createServerShips(): Server {
        for(i in 1..10) {
            when(i) {
                in 0..3 -> generateAutomaticShipArea(SUBMARINOS)
                in 3..6 -> generateAutomaticShipArea(CONTRATORPEDEIROS)
                in 6..8 -> generateAutomaticShipArea(NAVIO_TANQUE)
                in 8..9 -> generateAutomaticShipArea(PORTA_AVIAO)
            }
        }
        return this
    }


    private fun generateAutomaticShipArea(ship: Ships) {
        val initialPoint = getRandomPoint()
        val orientation = getRandomOrientation()

        if(!ValidationManager.validateShipArea(initialPoint,orientation,ship, gameManager.clientSea)) {
            generateAutomaticShipArea(ship)
            return
        }
        gameManager.setupShip(initialPoint,orientation,ship)
    }

    private fun getRandomOrientation(): Orientations {
        when((Math.random() * 3 + 1).toInt()) {
            1 -> return Orientations.NORTH
            2 -> return Orientations.SOUTH
            3 -> return Orientations.WEAST
            4 -> return Orientations.EAST
        }
        return Orientations.NORTH //unexpected
    }

    private fun getRandomInteger(): Int {
        return (Math.random() * 9).toInt()
    }

    private fun generateAttackCharSeq():String {
        return Integer.toString(getRandomInteger()) + Integer.toString(getRandomInteger())
    }

    fun startGame() {
        println("Waiting for Client")
//        println(gameManager.receiveGreatings())
        println("Start Game!")
        while(true) {
            gameManager.waitAttack().sendFeedback().doAttack(generateAttackCharSeq()).waitFeedback()
        }
    }
}