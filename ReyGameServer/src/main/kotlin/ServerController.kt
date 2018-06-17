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
        val propFile = File("properties.txt")

        if(propFile.isFile) {
            return loadProp()
        }
        else{
            generateProp()
            return loadProp()
        }
    }

    private fun loadProp() : Boolean{
        val propFile = File("properties.txt")
            propFile.useLines { lines ->
                lines.forEach {
                    if (it.isNotBlank()) {
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

    private fun generateProp() {
        val prop = HashMap<String,String>()
        val propFile = File("properties.txt")

        prop["port"] = "9999"
        prop["tick"] = "60"
        prop["bulletVelocity"] = "24"
        prop["playerRateX"] = "5"
        prop["playerRateY"] = "5"

        propFile.printWriter().use { out ->
            prop.forEach {
                out.println(it)
            }
        }
        window.display("File properties.txt generated correctly!")
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