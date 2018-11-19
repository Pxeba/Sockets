import java.io.InputStreamReader
import java.io.BufferedReader
import java.net.Socket
import GameManager.Companion.Ships
import GameManager.Companion.Ships.CONTRATORPEDEIROS
import GameManager.Companion.Ships.SUBMARINOS
import GameManager.Companion.Ships.PORTA_AVIAO
import GameManager.Companion.Ships.NAVIO_TANQUE
import GameManager.Companion.FeedbackTypes
import GameManager.Companion.FeedbackTypes.INVALID_ORIENTATION
import GameManager.Companion.FeedbackTypes.IMPOSSIBLE_AREA
import GameManager.Companion.FeedbackTypes.INVALID_AREA
import GameManager.Companion.FeedbackTypes.INVALID_INPUT
import GameManager.Companion.FeedbackTypes.INVALID_POSITION
import GameManager.Companion.Point
import GameManager.Companion.Orientations
import ValidationManager.Companion.userInputValidation
import ValidationManager.Companion.checkValidSeaPosition
import ValidationManager.Companion.validateShipArea
import java.net.InetAddress
import java.net.DatagramSocket;

fun main(args: Array<String>) {
    Client().createClientShips().startGame()
}

class Client {

    private val PORT = 6789
    private val IP = "127.0.0.1"
    private val clientSocket = Socket(IP, PORT)
    private val gameManager = GameManager(clientSocket)
    val userInput = BufferedReader(
            InputStreamReader(System.`in`))

    companion object {
        @JvmStatic
        fun Integer(char: Char): Int {
            return char.toInt() - 48
        }
    }

    fun createClientShips(): Client {
        for(i in 1..10) {
            when(i) {
                in 0..3 -> requestShipPositionInput(SUBMARINOS)
                in 3..6 -> requestShipPositionInput(CONTRATORPEDEIROS)
                in 6..8 -> requestShipPositionInput(NAVIO_TANQUE)
                in 8..9 -> requestShipPositionInput(PORTA_AVIAO)
            }
        }
        return this
    }

     private fun requestShipPositionInput(ship: Ships) {

         println("Choose a valid position:")
         println("range: 11 .. 99")
        val positionCharSeq = userInput.readLine()

         println("Choose a valid orientation: \n1-NORTH\n2-SOUTH\n3-WEAST\n4-EAST")
        val orientation = Orientations.fromInt(Integer(userInput.readLine()[0]))

        if(!userInputValidation(positionCharSeq)) {
            generateUserProperlyFeedback(INVALID_INPUT)
            requestShipPositionInput(ship) // re-create input
            return
        }

        val initialPoint = Point(Integer(positionCharSeq[0]),Integer(positionCharSeq[1]))
        if(!checkValidSeaPosition(initialPoint)) {
            generateUserProperlyFeedback(INVALID_POSITION)
            requestShipPositionInput(ship) // re-create input
            return
        }

        if(orientation == null) {
            generateUserProperlyFeedback(INVALID_ORIENTATION)
            requestShipPositionInput(ship) // re-create input
            return
        }

        if(!validateShipArea(initialPoint, orientation, ship, gameManager.clientSea)) {
            generateUserProperlyFeedback(INVALID_AREA)
            requestShipPositionInput(ship) // re-create input
            return
        }

        gameManager.setupShip(initialPoint, orientation, ship)  // ready to create ship
    }


    private fun generateUserProperlyFeedback(type: FeedbackTypes) {
        when(type) {
            INVALID_INPUT -> println("Invalid Input")
            IMPOSSIBLE_AREA -> println("Not Suficient Area inside Sea")
            INVALID_POSITION -> println("Invalid Position")
            INVALID_AREA -> println("Not Suficient Area inside Sea")
        }
    }

    fun startGame() {
//        gameManager.sendGreatings()
        println("position example: 37 (row: 3,column: 7)")
        while(true) {
            println("Choose a position to attack")
            val attackPointCharSeq = userInput.readLine()
            gameManager.doAttack(attackPointCharSeq).waitFeedback().waitAttack().sendFeedback()
        }
    }
}