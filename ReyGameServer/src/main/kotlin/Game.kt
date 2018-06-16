import kotlin.concurrent.thread


class Game {
    private var counter = -1
    private var counter_items = 0
    private var players = HashMap<Int, Player>()
    private var ids_to_ips = HashMap<Int, String>()
    private var ips_to_ids = HashMap<String, Int>()
    private var items = HashMap<Int, Item>()
    val commands = arrayListOf("Help","Players","Exit")

    init {
        // test item
        items[counter_items] = Item(300,400,3)
        counter_items++
        thread {
            while (true) {

            }
        }
    }

    fun ping(b : String) : ByteArray {
        val add = "b"
        return add.toByteArray() + b.toByteArray()
    }

    fun join(b : String, ip : String) : ByteArray {
        var add = "jr"
        if(ips_to_ids.containsKey(ip)){
            return add.toByteArray() + ips_to_ids[ip].toString().toByteArray()
        }
        //var id = 0
        counter++


        players[counter] = Player(counter,ip,b)
        ids_to_ips[counter] = ip
        ips_to_ids[ip] = counter
        add = "jc"
        window.display("$b joined")
        return add.toByteArray() + players[counter]!!.id.toString().toByteArray()
    }

    fun disconnect(ip : String) : ByteArray {
        try {
            val tempId = ips_to_ids[ip]
            ips_to_ids.remove(ip)
            ids_to_ips.remove(tempId)
            players.remove(tempId)
            return "dc".toByteArray()
        } catch (e : Exception){
            window.display(">>>Exception in $ip client on disconnect:\n$e")
        }
        return "dr".toByteArray()
    }

    fun firingVector(b : String, ip : String) : ByteArray {
        if(ips_to_ids.containsKey(ip))
            return "fc".toByteArray() + b.toByteArray()
        else
            return "fr".toByteArray() + b.toByteArray()
    }

    fun heal(b : String, ip : String) : ByteArray {
        try{
            players[ips_to_ids[ip]]!!.hp += b.takeWhile { it.isDigit() }.toInt()
            return "hc".toByteArray() + b.toByteArray()
        } catch (e : Exception){
            window.display(">>>Exception in $ip client on heal:\n$e")
        }
        return "hr".toByteArray() + b.toByteArray()
    }

    fun updatePos(b : String, ip : String) : ByteArray {
        try {
            val pos = b.split(",")
            val id = ips_to_ids[ip]
            println("${ips_to_ids[ip]} - $ip")
            players[id]!!.x = pos[0].takeWhile { it.isDigit() }.toInt()
            players[id]!!.y = pos[1].takeWhile { it.isDigit() }.toInt()
            return "pc".toByteArray() + b.toByteArray()
        } catch (e : Exception){
            window.display(">>>Exception in $ip client on update position:\n$e")
        }
        return "pr".toByteArray() + b.toByteArray()
    }

    fun pick(b : String, ip : String) : ByteArray {
        try {
            val a = b.split(",")
            val player = players[ips_to_ids[ip]]
            val radius = 50
            val id = a[0].takeWhile { it.isDigit() }.toInt()
            val slot = a[1].takeWhile { it.isDigit() }.toInt()
            if(player!!.x-radius < items[id]!!.x && items[id]!!.x < player.x+radius) {
                if (player.y - radius < items[id]!!.y && items[id]!!.y < player.y + radius) {
                    items[id]!!.pick(player)
                    if (player.inventory[slot] == null) {
                        player.inventory[slot] = items[id]
                        return "ic".toByteArray() + b.toByteArray()
                    }
                }
            }
        } catch (e : Exception){
            window.display(">>>Exception in $ip client on update position:\n$e")
        }
        return "ir".toByteArray() + b.toByteArray()
    }

    fun processCommand(command_:String) : String {
        val command = command_.takeWhile { it.isLetterOrDigit() }
        val transformCommands = ArrayList<String>()
        commands.forEach { transformCommands += it.toLowerCase() }
        if(transformCommands.contains(command.toLowerCase())){
            var output = ""
            when (transformCommands.indexOf(command.toLowerCase())){
                0 -> {
                    output += "Commands:\n"
                    commands.forEach { output += it + "\n"}
                    return output
                }
                1 -> {
                    output += "Players (${players.size}):\n"
                    players.forEach { _, u ->
                        output += u.nickname + "\n"
                    }
                    return output + "\n"
                }
                2 -> {
                    server.stopServer(0)
                    return "Server is shutting down!\n"
                }
            }
        }

        return "Invalid Command\n"
    }
}