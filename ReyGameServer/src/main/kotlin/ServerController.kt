import java.io.File
import java.net.ServerSocket
import kotlin.system.exitProcess


class ServerController {
    private var running = true
    private val serverThread = Thread(Runnable { mainLoop() })
    lateinit var game : Game
    val clients = HashMap<Int,ClientHandler>()
    val properties = HashMap<String,String>()

    init {
        // Server thread
        if(loadProperties()) {
            game = Game()
            serverThread.start()
        }
    }

    private fun mainLoop(){
        val server : ServerSocket
        val port = properties["port"]?.toInt() ?: 9999
        try {
            server = ServerSocket(port)
        } catch (e: Exception) {
            if (e.toString().contains("Address already in use", true))
                window.display(">>>Exception while starting server!\nAddress already in use")
            else
                window.display(">>>Unknown exception while starting server!\n$e")
            return
        }
        window.display("Server started on port $port!")
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
                        window.display(">>>Exception while loading properties.txt!\n$e")
                        return false
                    }
                }
            }
        }
        window.display("File properties.txt loaded correctly!")
        return true
    }

    fun stopServer(status : Int) {
        game.running = false
        clients.forEach {
            it.value.close()
        }
        running = false
        exitProcess(status)
    }
}