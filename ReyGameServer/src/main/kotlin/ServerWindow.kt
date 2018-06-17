import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import tornadofx.*

class ServerWindow : View("ReyGame server") {
    override val root : BorderPane by fxml("/views/ServerWindow.fxml")
    private val controller = window
    private val commandsWindow : TextArea by fxid("serverCommands")
    private val sendButton : Button by fxid("serverSendButton")
    private val commandLine : TextField by fxid("serverCommandLine")
    private val playersList : TextArea by fxid("serverPlayersList")

    init {
        playersList.bind(controller.players)
        commandsWindow.bind(controller.history)
        sendButton.setOnAction { send() }
        controller.history.onChange {
            commandsWindow.positionCaret(commandsWindow.length)
        }
        commandLine.setOnKeyPressed {
            if(it.code == KeyCode.ENTER)
                send()
        }
    }

    private fun send(){
        controller.history += "${controller.getTime()} ${commandLine.text}\n"
        controller.history += server.game.processCommand(commandLine.text)

        commandLine.clear()
        controller.save()
        commandsWindow.scrollTop = Double.MAX_VALUE
    }

    override fun onUndock() {
        server.stopServer(0)
        super.onUndock()

    }

}