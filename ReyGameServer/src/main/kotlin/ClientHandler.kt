import java.io.OutputStream
import java.net.Socket
import java.util.*

class ClientHandler(
        private val client : Socket,
        private val id : Int){
    private val reader = Scanner(client.getInputStream())
    private val writer : OutputStream = client.getOutputStream()
    private var running = false

    fun start() {
        running = true
        try {
            while (running) {    // client loop
                if (reader.hasNextLine()) {
                    val text = reader.nextLine()
                    if(handler(text)){
                        close()
                    }
                    println("Recived: $text")
                }
                client.keepAlive
                if (client.isClosed || !client.isConnected)
                    close()
            }
        } catch (e: Exception) {
            if(!e.toString().contains("Cocket is closed",true))
                window.display(">>>Exception in ${client.inetAddress.hostAddress} client while receiving data:\n$e")
        }
        close()
    }

    private fun send(message : ByteArray){
        writer.write(message)
    }

    fun close(){
        running = false
        client.close()
        window.display("Connection closed for ${client.inetAddress.hostAddress}")
        clients.remove(id)
    }

    private fun handler(message : String) : Boolean {
        val modifier = message[0].toByte()
        val onlyData = message.drop(1)//message.copyOfRange(1, message.size)
        //ping
        if (modifier == 97.toByte()) {
            val x = game.ping(onlyData)
            //println(x)
            send(x)
        }
        //join
        if (modifier == 106.toByte()) {
            val x = game.join(onlyData, client.inetAddress.hostAddress)
            //println(x)
            send(x)
        }
        //disconnect
        if (modifier == 100.toByte()) {
            val x = game.disconnect(client.inetAddress.hostAddress)
            //println(x)
            send(x)
            return true
        }
        //pos
        if (modifier == 112.toByte()) {
            val x = game.updatePos(onlyData, client.inetAddress.hostAddress)
            //println(x)
            send(x)
        }
        //firing_vector
        if (modifier == 102.toByte()) {
            val x = game.firingVector(onlyData, client.inetAddress.hostAddress)
            //println(x)
            send(x)
        }
        //heal
        if (modifier == 104.toByte()) {
            val x = game.heal(onlyData, client.inetAddress.hostAddress)
            //println(x)
            send(x)
        }
        if (modifier == 105.toByte()) {
            val x = game.pick(onlyData, client.inetAddress.hostAddress)
            //println(x)
            send(x)
        }
        return false
    }
}