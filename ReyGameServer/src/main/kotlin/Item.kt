


class Item(
        var x : Int,
        var y : Int,
        var typ : Int,
        var player: Player? = null,
        var picked : Boolean = false
){
    private var owner : Player? = player

    fun pick(player: Player) : Boolean{
        x = -600
        y = -600
        owner = player
        picked = true
        return true
    }
}