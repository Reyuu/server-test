import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import tornadofx.*

class ServerWindow : View("ReyGame server") {
    override val root : BorderPane by fxml("/views/ServerWindow.fxml")
    val controller = window
    private val commandsWindow : TextArea by fxid("serverCommands")
    private val sendButon : Button by fxid("serverSendButton")
    private val commandLine : TextField by fxid("serverCommandLine")

    init {
        commandsWindow.bind(controller.History)
        sendButon.setOnAction { send() }
        commandsWindow.setOnKeyTyped {
            commandsWindow.scrollTop = Double.MAX_VALUE
        }
        commandLine.setOnKeyPressed {
            if(it.code == KeyCode.ENTER)
                send()
        }
    }

    private fun send(){
        controller.History += "${controller.getTime()} ${commandLine.text}\n"
        controller.History += game.processCommand(commandLine.text)

        commandLine.clear()
        controller.save()
        commandsWindow.scrollTop = Double.MAX_VALUE
    }

    override fun onUndock() {
        server.stopServer(0)
        super.onUndock()

    }

}