import java.io.OutputStream
import java.net.Socket
import java.util.*

class ClientHandler(
        private val client : Socket,
        private val id : Int){
    private val reader = Scanner(client.getInputStream())
    private val writer : OutputStream = client.getOutputStream()
    private var running = true

    fun start() {
        try {
            while (running) {    // client loop
                if (reader.hasNextLine()) {
                    val text = reader.nextLine()
                    if(handler(text)){
                        client.close()
                    }
                    println("Received: $text")
                }
                if (client.isClosed || !client.isConnected)
                    running = false
            }
        } catch (e: Exception) {
            if(!(e.toString().contains("Socket is closed",true) || e.toString().contains("Socket closed",true)))
                window.display(">>>Exception while ${client.inetAddress.hostAddress} client receiving data:\n$e")
        }
        close()
    }

    private fun send(message : ByteArray){
        try {
            writer.write(message)
        } catch (e: Exception) {
            if(!(e.toString().contains("Socket is closed",true) || e.toString().contains("Socket closed",true)))
                window.display(">>>Exception while sending ${client.inetAddress.hostAddress} client data:\n$e")
        }
    }

    fun close(){
        running = false
        send("dc".toByteArray())
        if(!client.isClosed)
            client.close()
        window.playerUpdate("", client.inetAddress.hostAddress, false)
        window.display("Connection closed for ${client.inetAddress.hostAddress}")
        server.clients.remove(id)
    }

    private fun handler(message : String) : Boolean {
        val modifier = message[0].toByte()
        val onlyData = message.drop(1)//message.copyOfRange(1, message.size)
        val x : ByteArray
        //ping
        when (modifier) {
            97.toByte() -> {
                x = server.game.ping(onlyData)
                //println(x)
                send(x)
            }

        //join
            106.toByte() -> {
                x = server.game.join(onlyData, client.inetAddress.hostAddress)
                //println(x)
                send(x)
            }
        //disconnect
            100.toByte() -> {
                x = server.game.disconnect(client.inetAddress.hostAddress)
                //println(x)
                send(x)
                return true
            }
        //pos
            112.toByte() -> {
                x = server.game.updatePos(onlyData, client.inetAddress.hostAddress)
                //println(x)
                send(x)
            }
        //firing_vector
            102.toByte() -> {
                x = server.game.firingVector(onlyData, client.inetAddress.hostAddress)
                //println(x)
                send(x)
            }
        //heal
            104.toByte() -> {
                x = server.game.heal(onlyData, client.inetAddress.hostAddress)
                //println(x)
                send(x)
            }
            //pick item
            105.toByte() -> {
                x = server.game.pick(onlyData, client.inetAddress.hostAddress)
                //println(x)
                send(x)
            }
            //players list
            108.toByte() -> {
                x = server.game.playerList()
                send(x)
            }
            103.toByte() -> {
                x = server.game.getPosOfaPlayer(onlyData)
                send(x)
            }
            110.toByte() -> {
                x = server.game.getNicknameOfaPlayer(onlyData)
                send(x)
            }
            99.toByte() -> {
                x = server.game.checkForEvents()
                send(x)
            }
        }

        return false
    }
}