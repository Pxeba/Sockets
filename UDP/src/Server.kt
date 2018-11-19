import UDPMessenger.MessageTypes.*
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.lang.reflect.Array.getLength
import java.net.InetAddress


class Server: UDPMessenger {

    override var socket: DatagramSocket
    override var buf = ByteArray(1024)

    private lateinit var adress: InetAddress
    private var port: Int = 0



    constructor(port: Int) {
        socket = DatagramSocket(port)
    }

   fun receiveMessage() {
        val datagram = super.receiveUDPMessage()

        //Receber Endereçamento
        adress = datagram.address
        port = datagram.port


       println("Data vinda do disco " + getDataFromDisk())
    }

    fun sendMessage() {
        sendUDPMessage(lowestOil ?: "", NONE, adress, port)
        lowestOil = null
    }

    companion object {
        private val FILE_NAME = "disk.txt"
        private var fileReader = FileReader(FILE_NAME)
        private var fileWriter = FileWriter(FILE_NAME)
        private var bufferedReader = BufferedReader(fileReader)
        private var bufferedWriter = BufferedWriter(fileWriter)
        var lowestOil: Int? = null

        @JvmStatic
        fun getDataFromDisk(): MutableList<Data> {
            var dataArray = mutableListOf<Data>()
            val iterator = bufferedReader.lineSequence().iterator()
            while(iterator.hasNext()) {
                dataArray.add(Data.getDataFromString(iterator.next()))
            }
            return dataArray
        }

        @JvmStatic
        fun saveData(data: Data) {
            bufferedWriter.write(data.toString())
            bufferedWriter.newLine()
            bufferedWriter.flush()

//        bufferedWriter.close()
        }

        @JvmStatic
        fun findLowestOil(coordenate: Coordenate, range: Int) {
            val dataList: MutableList<Data> = getDataFromDisk()
            var currentLowestPrice: Int? = null
            for (i in 0 until dataList.count()) {
                if (Math.sqrt( Math.pow((dataList.get(i).coord.yCoord - coordenate.yCoord).toDouble(),2.0)
                                - Math.pow((dataList.get(i).coord.xCoord - coordenate.xCoord).toDouble(),2.0)) < range) {
                    currentLowestPrice = dataList[i].price
                }
            }
            lowestOil = currentLowestPrice
        }
    }




}