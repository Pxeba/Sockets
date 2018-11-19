import Server.Companion.findLowestOil
import Server.Companion.saveData
import UDPMessenger.MessageTypes.*
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

open class UDPMessenger  {

    open var buf = ByteArray(1024)
    open lateinit var socket: DatagramSocket

    enum class MessageTypes(val type: Int){
        DATA(0), SEARCH(1), OIL_PRICE(2), NONE(3)
    }

    class Data: Serializable {
        var id: Int
        var oilType: Int
        var price: Int
        var coord: Coordenate


        constructor(id: Int, oilType: Int, price: Int, coord: Coordenate){
            this.id = id
            this.oilType = oilType
            this.price = price
            this.coord = coord
        }

        override fun toString(): String {
            return "D" + id.toString() + "." + oilType.toString() + "." +
                    price.toString() + "." + coord.xCoord.toString() + "." + coord.yCoord
        }

        companion object {
            @JvmStatic
            fun getDataFromString(dataCharSeq: String): Data {
                val attributes: List<String> =  dataCharSeq.split(".")

                val id = attributes[0].replace("D","").toInt()
                val oilType = attributes[1].toInt()
                val price = attributes[2].toInt()
                val coord =  Coordenate(attributes[3].toInt(), attributes[4].toInt())

                return Data(id,oilType,price,coord)
            }
        }

    }


    class Search: Serializable {
        var id: Int
        var oilType: Int
        var range: Int
        var coord: Coordenate

        constructor(id: Int, oilType: Int, range: Int, coord: Coordenate) {
            this.id = id
            this.oilType = oilType
            this.range = range
            this.coord = coord
        }

        override fun toString(): String {
            return "P" + id.toString() + "." + oilType.toString() + "." +
                    range.toString() + "." + coord.xCoord.toString() + "." + coord.yCoord
        }
    }

    data class Coordenate(val xCoord: Int, val yCoord: Int) : Serializable

    constructor() { } //do nothing

    fun sendUDPMessage(message: Any, messageType: MessageTypes,adress: InetAddress, port: Int) {
        lateinit var msgObj: Any
        when(messageType) {
            DATA -> msgObj = message as Data
            SEARCH -> msgObj = message as Search
            OIL_PRICE -> msgObj = message as Int
            NONE -> msgObj = message as String
        }
        //Transform Object into Byte array
        val outputByteArray = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(outputByteArray)

        objectOutputStream.writeObject(msgObj)
        buf = outputByteArray.toByteArray()

        val datagram = DatagramPacket(buf, buf.count(), adress, port)
        socket.send(datagram)
    }

    open fun receiveUDPMessage(): DatagramPacket {
        val datagram = DatagramPacket(buf, buf.count())
        socket.receive(datagram)

        //Transform into Object
        var dataByteArray: ByteArray = datagram.data
        val inputByteArray = ByteArrayInputStream(dataByteArray)
        val objectInput = ObjectInputStream(inputByteArray).readObject()

        if(objectInput is Data) {
            println("Data send from Client" + objectInput)
            saveData(objectInput)
        } else if(objectInput is Search) {
            println("Search send from Client" + objectInput)
            findLowestOil(objectInput.coord, objectInput.range)
        } else if(objectInput is Int) {
            println("Lowest Oil send from Server" + objectInput as Int)
        }

        return datagram
    }
}