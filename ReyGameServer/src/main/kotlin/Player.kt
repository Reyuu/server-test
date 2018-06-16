


class Player(
        val id : Int,
        val ip : String,
        var nickname : String,
        var x : Int = 0,
        var y : Int = 0,
        var hp : Int = 100) {
    private var x_rate = 5
    private var y_rate = 5
    var inventory = HashMap<Int,Item?>()

    init {
        for(i in 0..9)
            inventory[i] = null
    }
}