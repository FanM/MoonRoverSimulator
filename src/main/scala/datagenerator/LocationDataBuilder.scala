package datagenerator

class LocationData (
  val id: Int,
  val direction: Double, // [0, 2*Pi)
  val speed: Int,
  val position: (Double, Double) = (0, 0),
  val turnAngle: Double = 0) {

  import LocationData._

  /**
    *
    * @return String
    * Builds one LocationData as
    * id direction speed position.x position.y turnAngle
    */
  def serialize(): String = {
    val sBuilder = new StringBuilder
    sBuilder.append(id)
    sBuilder.append(DELIMITER)
    sBuilder.append(direction)
    sBuilder.append(DELIMITER)
    sBuilder.append(speed)
    sBuilder.append(DELIMITER)
    sBuilder.append(position._1)
    sBuilder.append(DELIMITER)
    sBuilder.append(position._2)
    sBuilder.append(DELIMITER)
    sBuilder.append(turnAngle)
    sBuilder.toString()
  }
}

object LocationData {
  final val DELIMITER = '\t'

  def deserialize(objStr: String): LocationData = {
    val objArr = objStr.split(DELIMITER)
    new LocationData(
      id = objArr(0).toInt,
      direction = objArr(1).toDouble,
      speed = objArr(2).toInt,
      position = (objArr(3).toDouble, objArr(4).toDouble),
      turnAngle = objArr(5).toDouble
    )
  }
}

class LocationDataBuilder (val initPosition: (Double, Double),
                           val initAngle: Double,
                           val speedLimit: Int,
                           val duration: Int) {

  def buildDataSequence(id:Int, rng: RNG): List[LocationData] = {

    // Let's confine the turning angle to [-Pi/2, +Pi/2] to make the turns less random
    val turnAngleList = RandomNumberGenerator.doubles(duration)
                          .run(rng)._1.map(a => (a - 0.5) * Math.PI)
    // Also add a speed limit to make the speed change less random
    val speedList = RandomNumberGenerator.nonNegativeLessThanList(speedLimit, duration).run(rng)._1

    var nextPosition = initPosition
    var direction = getDirection(0, initAngle)

    (turnAngleList zip speedList).map {
      case (a, s) =>
        val locationData = new LocationData(
          id = id,
          turnAngle = a,
          speed = s,
          position = nextPosition,
          direction = direction)
        nextPosition = (nextPosition._1 + s * Math.cos(direction), nextPosition._2 + s * Math.sin(direction))
        direction = getDirection(direction, a)

        locationData
    }
  }

  // gets the new direction counterclockwise
  def getDirection(oldDirection: Double, turnAngle: Double): Double = {
    val twoPi = 2 * Math.PI
    var newDirection = oldDirection - turnAngle // turn counterclockwise
    if (newDirection < 0)
      newDirection += twoPi
    else if (newDirection >= twoPi)
      newDirection -= twoPi
    newDirection
  }
}
