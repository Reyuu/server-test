import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.File
import java.time.LocalDateTime


class ServerWindowController : Controller() {
    val History : SimpleStringProperty

    init {
        History = SimpleStringProperty("${getTime()} Server started!\n")
    }

    fun display(message:String){
        if(message.take(3).contains(">>>"))
            History += "${getTime()} ERROR!\n${message.drop(3)}\n"
        else
            History += "${getTime()} $message\n"
        save()
    }

    fun save(){
        File("LastLog.txt").printWriter().use { out ->
            History.get().split("\n").forEach {
                out.println(it)
            }
        }
    }

    fun getTime() : String{
        val day = if(LocalDateTime.now().dayOfMonth < 10) "0" + LocalDateTime.now().dayOfMonth.toString()
        else LocalDateTime.now().dayOfMonth.toString()
        val month = if(LocalDateTime.now().monthValue < 10) "0" + LocalDateTime.now().monthValue.toString()
        else LocalDateTime.now().monthValue.toString()
        val year = if(LocalDateTime.now().year < 10) "0" + LocalDateTime.now().year.toString()
        else LocalDateTime.now().year.toString()
        val hour = if(LocalDateTime.now().hour < 10) "0" + LocalDateTime.now().hour.toString()
        else LocalDateTime.now().hour.toString()
        val minute = if(LocalDateTime.now().minute < 10) "0" + LocalDateTime.now().minute.toString()
        else LocalDateTime.now().minute.toString()
        val second = if(LocalDateTime.now().second < 10) "0" + LocalDateTime.now().second.toString()
        else LocalDateTime.now().second.toString()
        return "[$day-$month-$year $hour:$minute:$second]"
    }
}