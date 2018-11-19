import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.ObjectOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import UDPMessenger.MessageTypes.SEARCH
import UDPMessenger.MessageTypes.DATA


class Client: UDPMessenger {

    private var port: Int
    private var adress: InetAddress
    override var socket: DatagramSocket
    override var buf = ByteArray(1024)

    private var data: Data? = null
    private var search: Search? = null
    private var msgType: MessageTypes? = null
    val map: MutableMap<Int, MessageTypes> = HashMap()

    val userInput = BufferedReader(InputStreamReader(System.`in`))
    val cont = 1

    constructor(port: Int, adress: InetAddress) {
        this.port = port
        this.adress = adress
        socket = DatagramSocket()

        for (i in MessageTypes.values()) {
            map[i.type] = i
        }
    }

    fun getMessageTypeInput(): MessageTypes {
        System.out.println("Escreva um tipo de mensagem")
        System.out.println(" 0 - Dado, 1 - Busca")
        val type = Integer.parseInt(userInput.readLine())

        return if(map[type] != null ) { map[type]!! } else { getMessageTypeInput() } // kotlin s2
    }

    fun getDataInput(): Data? {
        System.out.println("Choose oil type")
        System.out.println(" 0 - diesel, 1 - álcool, 2- gasolina ")
        val oil = Integer.parseInt(userInput.readLine())
        System.out.println("Choose a price between 1 and 100")
        val price = Integer.parseInt(userInput.readLine())
        System.out.println("Choose a x Coordenate between 1 and 999")
        val coordX = Integer.parseInt(userInput.readLine())
        System.out.println("Choose a y Coordanete between 1 and 999")
        val coordY = Integer.parseInt(userInput.readLine())


        if(dadosInputValidation(oil, price, coordX, coordY)) {
            return Data((Math.random() * 100).toInt(), oil, price, Coordenate(coordX, coordY))
        }

        return null
    }

    private fun getSearchInput(): Search? {
        System.out.println("Choose search type")
        System.out.println(" 0 - diesel, 1 - álcool, 2- gasolina ")
        val oil = Integer.parseInt(userInput.readLine())
        System.out.println("Choose a range between 1 and 100")
        val range = Integer.parseInt(userInput.readLine())
        System.out.println("Choose a x Coordenate between 1 and 999")
        val coordX = Integer.parseInt(userInput.readLine())
        System.out.println("Choose a y Coordanete between 1 and 999")
        val coordY = Integer.parseInt(userInput.readLine())

        if(dadosInputValidation(oil, range, coordX, coordY)) {
            return Search((Math.random() * 100).toInt(), oil, range, Coordenate(coordX, coordY))
        }

        return null
    }

    fun setupMessage(): Client {
        setupMessage(getMessageTypeInput())
        return this
    }

    fun setupMessage(messageType: MessageTypes) {
        if(messageType == SEARCH) {
            search = getSearchInput()

            search.let {
                msgType = SEARCH
            }

        } else if(messageType == DATA) {
            data = getDataInput()

            data.let {
                msgType = DATA
            }
        }

        if(msgType == null) {
            setupMessage(messageType) // request user write again
        }
    }

    private fun dadosInputValidation(oil: Int, priceOrRange: Int, coordX: Int, coordY: Int): Boolean {
        if( (oil in 0..2) && (priceOrRange in 1..100) &&
                (coordX in 1..999) && (coordY in 1..999) ) {
            return true
        }

        return false
    }

    fun sendMessage() {
        when(msgType) {
            DATA -> sendUDPMessage(data!!, msgType!!, adress, port)
            SEARCH -> sendUDPMessage(search!!, msgType!!, adress, port)
        }
    }

    fun receiveMessage() {
        receiveUDPMessage()
        msgType = null
    }

    fun waitConfirmation() {

    }


    fun printMessages() {

    }

    fun generateRandomData() {

    }

    fun generateRandomSearch() {

    }

    fun getRandomInteger() {

    }

}