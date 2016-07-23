package model

import datagenerator.LocationData

/**
  * Moon Rover
  * Reads LocationData sequence and sends them to CommandCenter every 1 second
  */
class MoonRover(val id: Int,
                val locationDataSequence: Vector[LocationData],
                val commandCenter: CommandCenter,
                initialDelay: Long,
                interval: Long)
  extends PeriodicTaskRunner (initialDelay, interval){

  var messageCounter = 0

  def runTask(): Unit = {
    if (messageCounter < locationDataSequence.size) {
      commandCenter.getMessageReceiver ! locationDataSequence(messageCounter)
      messageCounter += 1
    } else {
      commandCenter.getMessageReceiver ! None
      println("Shutting down Rover " + id)
      abort()
    }
  }
}
