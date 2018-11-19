import ValidationManager.Companion.checkValidSeaPosition
import ValidationManager.Companion.userInputValidation
import ValidationManager.Companion.checkIfPointHaveShip
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket

class GameManager {

    companion object {
        enum class Ships(val size: Int) {
            PORTA_AVIAO(5), NAVIO_TANQUE(4), CONTRATORPEDEIROS(3), SUBMARINOS(2)
        }

        enum class Orientations(val input: Int) {
            NORTH(1), SOUTH(2), WEAST(3), EAST(4);

            companion object {
                val map: MutableMap<Int, Orientations> = HashMap()

                init {
                    for (i in Orientations.values()) {
                        map[i.input] = i
                    }
                }

                fun fromInt(type: Int): Orientations? {
                    return map[type]
                }
            }
        }

        enum class FeedbackTypes {
            INVALID_INPUT, IMPOSSIBLE_AREA, INVALID_POSITION, INVALID_AREA, INVALID_ORIENTATION
        }

        data class Point(val row: Int, val column: Int)
        fun Point(charSeq: String): Point {
            return if(charSeq.count() != 2) {
                Point(-1,-1)
            } else {
                Point(Integer(charSeq.get(0)), Integer(charSeq.get(1)))
            }
        }

        fun Integer(char: Char): Int {
            return char.toInt() - 48
        }

        fun getShipEndpoint(initialPoint: Point, orient: Orientations?, ship: Ships): Point {
            when(orient) {
                Orientations.NORTH -> return Point(initialPoint.row - ship.size, initialPoint.column)
                Orientations.SOUTH -> return Point(initialPoint.row + ship.size, initialPoint.column)
                Orientations.WEAST -> return Point(initialPoint.row, initialPoint.column + ship.size)
                Orientations.EAST -> return Point(initialPoint.row, initialPoint.column - ship.size)
                else -> return initialPoint // not expected return
            }
        }
    }


    constructor(socket: Socket) {
        connectionSocket = socket
        fromEnemyMsg = BufferedReader(InputStreamReader(connectionSocket.getInputStream()))
        toEnemyMsg = DataOutputStream(connectionSocket.getOutputStream())
    }

    var clientSea = Array(10 ) { arrayOfNulls<Int>(10) }
    private var enemySea = Array(10 ) { arrayOfNulls<Int>(10) }
    private val connectionSocket: Socket
    val fromEnemyMsg: BufferedReader
    val toEnemyMsg: DataOutputStream
    lateinit var commingAttack: Point
    lateinit var lastAttackPoint: Point

    fun setupShip(initialPoint: Point, orient: Orientations?, ship: Ships) {
        var endPoint: Point = getShipEndpoint(initialPoint, orient, ship)
        drawShip(initialPoint, endPoint)
    }

    private fun drawShip(initial: Point, end: Point) {
        for(i in initial.row..end.row) {
            for(j in initial.column..end.column) {
                clientSea[i][j] = 1
            }
        }
        showClientCurrentSea()
    }

    private fun showClientCurrentSea() {
        for(i in 0..9) {
            for(j in 0..9) {
                print(if(clientSea[i][j]== null){"0 "} else {"X "})
            }
            println("")
        }
    }

    fun doAttack(attackPointCharSeq: String): GameManager {
        if(checkValidSeaPosition(Point(attackPointCharSeq))) {
            toEnemyMsg.writeBytes(attackPointCharSeq + "\n")
            toEnemyMsg.flush()
        }
        else {
            println("Invalid attack, you lost your round")
            toEnemyMsg.writeBytes("" + "\n")
            toEnemyMsg.flush()
        }

        lastAttackPoint = Point(attackPointCharSeq)
        return this
    }

    fun waitFeedback(): GameManager {
        val feedback = fromEnemyMsg.readLine()
        System.out.println("Result from server: " + feedback)
        updateEnemySea(feedback.equals("hit")) // Update sea

        return this
    }

    fun updateEnemySea(didHit: Boolean) {
        if(didHit) {
            enemySea[lastAttackPoint.row][lastAttackPoint.column] = 0
        } else {
            enemySea[lastAttackPoint.row][lastAttackPoint.column] = 1
        }
    }

    fun waitAttack(): GameManager {
        val attackPositionCharSeq = fromEnemyMsg.readLine()
        println("Hit from server: $attackPositionCharSeq")
        receiveAttack(Point(attackPositionCharSeq))

        return this
    }

    private fun checkIfPointHaveShip(point: Point): Boolean {
        if(clientSea[point.row][point.column] == null) {
            return false
        }
        return true
    }

    private fun receiveAttack(attackPoint: Point) {
        if(checkIfPointHaveShip(attackPoint)) {
            clientSea[attackPoint.row][attackPoint.column] = null
        }
        commingAttack = attackPoint
    }

    fun sendFeedback(): GameManager {
        var attackFeedback = if(checkIfPointHaveShip(commingAttack)) {
            "hit\n"
        } else {
            "miss\n"
        }

        toEnemyMsg.writeBytes(attackFeedback)
        toEnemyMsg.flush()
        return this
    }

    fun sendGreatings() {
        toEnemyMsg.writeBytes("Client Successful conected")
        toEnemyMsg.flush()
    }

    fun receiveGreatings(): String {
        return fromEnemyMsg.readLine()
    }
}