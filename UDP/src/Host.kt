import java.net.InetAddress
import UDPMessenger.MessageTypes.DATA
import UDPMessenger.MessageTypes.SEARCH
import UDPMessenger.MessageTypes.OIL_PRICE
import java.io.BufferedReader
import java.io.InputStreamReader

fun main(args: Array<String>) {
    Host().run()

}

class Host: Thread() {

    private lateinit var currentClient: Client
    private var client1: Client? = null
    private var client2: Client? = null
    private var client3: Client? = null
    private var server = Server(7777)

    private val userInput = BufferedReader(InputStreamReader(System.`in`))

    override fun run() {

        while (true) {
            val clientId = getClientFromInput()
            connectSelectedClient(clientId)

            // do communication
            sendClientToServerMsg().sendServerToClientMsg()
        }
    }

    fun getClientFromInput(): Int {
        println("Escolha um cliente para se comunicar com o Servidor:")
        println("0-Client1, 1-Client2, 2-Client3")
        val clientId = Integer.parseInt(userInput.readLine())
        return if(clientId in 0..2) {
            clientId
        } else {
            return getClientFromInput()
        }
    }

    fun sendClientToServerMsg(): Host {
        currentClient.setupMessage().sendMessage()
        server.receiveMessage()

        return this
    }

    fun sendServerToClientMsg(): Host {
        server.sendMessage()
        currentClient.receiveMessage()

        return this
    }

    fun disconnectClients() {
        client1 = null; client2 = null; client3 = null
    }

    fun connectSelectedClient(id: Int) {
        if(id == 0) {
            client1 = Client(7777, InetAddress.getByName("localhost"))
            currentClient = client1!!
        } else if(id == 1) {
            client2 = Client(7777, InetAddress.getByName("localhost"))
            currentClient = client2!!
        } else if(id == 2){
            client3 = Client(7777, InetAddress.getByName("localhost"))
            currentClient = client3!!
        }
    }

}