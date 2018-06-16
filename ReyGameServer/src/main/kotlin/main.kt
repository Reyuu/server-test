import tornadofx.*
import kotlin.collections.HashMap

var server = ServerController()
val window = ServerWindowController()
val game = Game()
val clients = HashMap<Int,ClientHandler>()

fun main(args: Array<String>) {
    launch<ServerApp>(args)
}