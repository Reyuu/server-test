import tornadofx.*
import kotlin.concurrent.thread


class Game {
    private var players = HashMap<Int, Player>()
    private var idsToIps = HashMap<Int, String>()
    private var ipsToIds = HashMap<String, Int>()
    private var items = HashMap<Int, Item>()
    private val commands = HashMap<String,String>()
    private val query = HashMap<String, List<String>>()

    var running = true

    init {
        // test item
        commands["Help"] = "Displays this message"
        commands["Exit"] = "Shutdowns server"
        commands["Players"] = "Displays players list"
        commands["Kick <player>"] = "Disconnects player from server"
        commands["Give <itemID> <player>"] = "Gives item with entered id to player"
        commands["di <player>"] = "Displays current player inventory"
        items[items.size] = Item(300, 400, 3)
        tickLoop()
    }

    private fun tickLoop(){
        thread {
        val tick = server.properties["tick"]?.toLong() ?: 60
            while (running) {
                query.forEach { t, u ->
                    try {
                        when (t) {
                            "updatePlayerVelocity" -> {
                                // u[0] - id   u[1] - xVel   u[2] - yVel
                                val xR = players[u[0].toInt()]!!.xRate
                                val yR = players[u[0].toInt()]!!.yRate
                                players[u[0].toInt()]!!.xVelocity = if (u[1].toInt() >= -xR && u[1].toInt() <= xR) {
                                    u[1].toInt()
                                } else {
                                    players[u[0].toInt()]!!.xVelocity
                                }

                                players[u[0].toInt()]!!.yVelocity = if (u[2].toInt() >= -yR && u[2].toInt() <= yR) {
                                    u[2].toInt()
                                } else {
                                    players[u[0].toInt()]!!.yVelocity
                                }

                                players[u[0].toInt()]!!.updatePosition()
                            }
                            "updateBulletPosition" -> {
                                players[u[1].takeWhile { it.isDigit() }.toInt()]?.bullets?.forEach {
                                    it.updatePosition()
                                }
                            }
                        }
                    } catch (e:Exception){
                        window.display(">>>Exception in while processing $t:\n$e")
                    }
                }
                query.clear()
                Thread.sleep(1000 / tick)
            }
        }
    }

    fun ping(b: String): ByteArray {
        val add = "b"
        return add.toByteArray() + b.toByteArray()
    }

    fun join(b: String, ip: String): ByteArray {
        var add = "jr"
        if (ipsToIds.containsKey(ip)) {
            return add.toByteArray() + ipsToIds[ip].toString().toByteArray()
        }
        var id = 0
        for (i in 0..24) {
            if (!players.containsKey(i)) {
                id = i
                break
            }
        }

        players[id] = Player(id, ip, b)
        idsToIps[id] = ip
        ipsToIds[ip] = id
        add = "jc"
        window.playerUpdate(b, ip, true)
        window.display("$b joined")
        return add.toByteArray() + players[id]!!.id.toString().toByteArray()
    }

    fun disconnect(ip: String): ByteArray {
        try {
            val tempId = ipsToIds[ip]
            val nickname = players[tempId]!!.nickname
            ipsToIds.remove(ip)
            idsToIps.remove(tempId)
            players.remove(tempId)
            //window.playerUpdate("", ip, false)
            window.display("$nickname disconnected")
            return "dc".toByteArray()
        } catch (e: Exception) {
            window.display(">>>Exception in $ip client on disconnect:\n$e")
        }
        return "dr".toByteArray()
    }

    fun firingVector(b: String, ip: String): ByteArray {
        return when (ipsToIds.containsKey(ip)) {
            true -> {
                players[ipsToIds[ip]]!!.bullets.add(Bullet(b,players[ipsToIds[ip]]!!.x,players[ipsToIds[ip]]!!.y))
                "fc".toByteArray() + b.toByteArray()
            }
            false -> "fr".toByteArray() + b.toByteArray()
        }
    }

    fun heal(b: String, ip: String): ByteArray {
        try {
            players[ipsToIds[ip]]!!.hp += b.takeWhile { it.isDigit() }.toInt()
            return "hc".toByteArray() + b.toByteArray()
        } catch (e: Exception) {
            window.display(">>>Exception in $ip client on heal:\n$e")
        }
        return "hr".toByteArray() + b.toByteArray()
    }

    fun updatePos(b: String, ip: String): ByteArray {
        try {
            val pos = b.split(",")
            val id = ipsToIds[ip]
            println("${ipsToIds[ip]} - $ip")
            players[id]!!.x = pos[0].takeWhile { it.isDigit() }.toInt()
            players[id]!!.y = pos[1].takeWhile { it.isDigit() }.toInt()
            return "pc".toByteArray() + b.toByteArray()
        } catch (e: Exception) {
            window.display(">>>Exception in $ip client on update position:\n$e")
        }
        return "pr".toByteArray() + b.toByteArray()
    }

    fun pick(b: String, ip: String): ByteArray {
        try {
            val a = b.split(",")
            val player = players[ipsToIds[ip]]
            val radius = 50
            val id = a[0].takeWhile { it.isDigit() }.toInt()
            val slot = a[1].takeWhile { it.isDigit() }.toInt()
            if (player!!.x - radius < items[id]!!.x && items[id]!!.x < player.x + radius) {
                if (player.y - radius < items[id]!!.y && items[id]!!.y < player.y + radius) {
                    items[id]!!.pick(player)
                    if (player.inventory[slot] == null) {
                        player.inventory[slot] = items[id]
                        return "ic".toByteArray() + b.toByteArray()
                    }
                }
            }
        } catch (e: Exception) {
            window.display(">>>Exception in $ip client on item pick:\n$e")
        }
        return "ir".toByteArray() + b.toByteArray()
    }

    fun playerList(): ByteArray {
        if (players.isNotEmpty()) {
            return "lc".toByteArray() + ",${players.keys.drop(1).dropLast(1)}".toByteArray()
        }
        return "lr".toByteArray()
    }

    fun getPosOfaPlayer(id_: String): ByteArray {
        if (id_.takeWhile { it.isDigit() }.isNotBlank()) {
            val id = id_.takeWhile { it.isDigit() }.toInt()
            try {
                return "gc${players[id]?.x},${players[id]?.y}".toByteArray()
            } catch (e: Exception) {
                window.display(">>>Exception while getting position of a player id: $id:\n$e")
            }
        } else
            window.display(">>>Exception while getting position of a player\nId is empty")
        return "gr$id_".toByteArray()
    }

    fun getNicknameOfaPlayer(id_: String): ByteArray {
        if (id_.takeWhile { it.isDigit() }.isNotBlank()) {
            val id = id_.takeWhile { it.isDigit() }.toInt()
            try {
                return "nc${players[id]?.nickname}".toByteArray()
            } catch (e: Exception) {
                window.display(">>>Exception while getting nickname of a player id: $id:\n$e")
            }
        } else
            window.display(">>>Exception while getting nickname of a player\nId is empty")
        return "nr$id_".toByteArray()
    }

    fun checkForEvents() : ByteArray{
        return "cr".toByteArray()
    }

    fun processCommand(command_: String): String {
        val command = command_.split(" ")
        if(command.isEmpty())
            return "Invalid Command\n"
        val transformCommands = ArrayList<String>()
        commands.forEach { transformCommands += it.key.toLowerCase().takeWhile { it.isLetterOrDigit() } }
        if (transformCommands.contains(command[0].toLowerCase())) {
            var output = ""
            when (command[0].toLowerCase()) {
                "help" -> {
                    output += "Commands:\n"
                    commands.forEach { t, u ->
                        output += "$t = $u\n"
                    }
                    return output
                }
                "exit" -> {
                    server.stopServer(0)
                    return "Server is shutting down!\n"
                }
                "players" -> {
                    output += "Players (${players.size}):\n"
                    players.forEach { _, u ->
                        output += u.nickname + "\n"
                    }
                    return output + "\n"
                }
                "kick" -> {
                    var id = -1
                    players.forEach { _, u ->
                        if (u.nickname == command[1]) {
                            id = u.id
                            return@forEach
                        }
                    }
                    if (id >= 0)
                        try {
                            disconnect(idsToIps[id]!!)
                            return ""
                        } catch (e: Exception) {
                            window.display("<<<Exception while attempting to disconnect $output:\n$e")
                        }
                    else
                        return "Invalid user"
                }
                "give" -> {
                    if(!command[1].isInt())
                        return "Invalid item id!\n"
                    if(!items.containsKey(command[1].toInt()))
                        return "Item with id ${command[1]} doesn't exist!\n"
                    var id = -1
                    players.forEach { t, u ->
                        if(u.nickname == command[2]){
                            id = t
                            return@forEach
                        }
                    }
                    if(id < 0)
                        return "Player ${command[2]} doesn't exist!\n"

                    var slot = -1
                    players[id]?.inventory?.forEach { t, u ->
                        if(u == null){
                            slot = t
                            return@forEach
                        }
                    }
                    if(slot < 0)
                        return "Player ${command[2]} doesn't have empty space in inventory!\n"

                    players[id]!!.inventory[slot] = items[command[1].toInt()]
                    return "Player ${command[2]} received item with id ${command[1]}"
                }
                "di" -> {
                    var id = -1
                    players.forEach { t, u ->
                        if(u.nickname == command[1]){
                            id = t
                            output += "${u.nickname}'s inventory:\n"
                            u.inventory.forEach { t, u ->
                                if(u == null)
                                    output += "$t = none\n"
                                else
                                    output += "$t = $u\n"
                            }
                            return@forEach
                        }
                    }
                    return if(id < 0)
                        "Player ${command[1]} doesn't exist!\n"
                    else
                        output
                }
            }
        }

        return "Invalid Command\n"
    }
}