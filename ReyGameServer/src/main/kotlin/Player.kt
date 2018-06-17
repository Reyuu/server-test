import java.util.*
import kotlin.collections.ArrayList

class Player(
        val id: Int,
        val ip: String,
        var nickname: String,
        var x: Int = 0,
        var y: Int = 0,
        var hp: Int = 100) {
    val bullets = ArrayList<Bullet>()
    var xVelocity = 0
    var yVelocity = 0
    val xRate = server.properties["playerRateX"]?.toInt() ?: 5
    val yRate = server.properties["playerRateY"]?.toInt() ?: 5
    var inventory = HashMap<Int, Item?>()

    init {
        for (i in 0..9)
            inventory[i] = null
    }

    fun updatePosition() {
        x += xVelocity
        y += yVelocity
    }

    fun collideWith(x: Int, y: Int): Boolean {
        val collisionBox = arrayListOf(x - 25, x + 25, y - 25, y + 25)
        return x > collisionBox[0] &&
                x < collisionBox[1] &&
                y > collisionBox[2] &&
                y < collisionBox[3]
    }
}