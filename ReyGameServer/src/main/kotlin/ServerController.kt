import java.io.File
import java.net.ServerSocket
import kotlin.system.exitProcess


class ServerController {
    var running = true
    val properties = HashMap<String,String>()
    val serverThread = Thread(Runnable { mainLoop() })

    init {
        // Server thread
        if(loadProperties())
            serverThread.start()
    }

    private fun mainLoop(){
        val server : ServerSocket
        try {
            server = ServerSocket(properties["port"]!!.toInt())
        } catch (e: Exception) {
            if (e.toString().contains("Address already in use", true))
                window.display(">>>Exception while starting server!\nAddress already in use")
            else
                window.display(">>>Unknown exception while starting server!\n$e")
            return
        }

        while (running) {
            val client = server.accept()
            window.display("Client connected: ${client.inetAddress.hostAddress}")
            val handler = ClientHandler(client, clients.size)
            var id = 0
            while (clients.containsKey(id)) id++
            clients[id] = handler
            clients[id]!!.start()
        }
    }

    private fun loadProperties() : Boolean {
        File("properties.txt").useLines { lines ->
            lines.forEach {
                if(it.isNotBlank()) {
                    val property = it.replace(" ", "").split("=")
                    println(property)
                    try {
                        properties[property[0]] = property[1]
                    } catch (e: Exception) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun stopServer(status : Int) {
        running = false
        clients.forEach {
            it.value.close()
        }
        exitProcess(status)
    }
}