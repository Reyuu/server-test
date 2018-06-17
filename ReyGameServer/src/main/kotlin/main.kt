import tornadofx.*
import kotlin.collections.HashMap

val window = ServerWindowController()
val server = ServerController()

fun main(args: Array<String>) {
    launch<ServerApp>(args)
}