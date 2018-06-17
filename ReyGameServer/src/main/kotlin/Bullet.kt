import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class Bullet (
        var firingVector : String,
        var x : Int,
        var y : Int
){
    private val velocity = server.properties["bulletVelocity"]?.toInt() ?: 24

    fun updatePosition(){
        val firingVectorX = firingVector.takeWhile { it != ',' }.toDouble()
        val firingVectorY = firingVector.takeLastWhile { it != ',' }.toDouble()
        val c = sqrt(firingVectorX.pow(2) + firingVectorY.pow(2))

        x += (firingVectorX/c * velocity).roundToInt()
        y += (firingVectorY/c * velocity).roundToInt()
    }
}