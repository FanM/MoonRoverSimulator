package model

import akka.actor.{Actor, Props}
import datagenerator.LocationData

/**
  * Command Center
  * Uses one MessageReceiver to receive the data from MoonRovers and stores them in a buffer array
  * Prints the data from the buffer array every interval millisecond
  */
class CommandCenter(roverCount: Int,
                    transmissionDelayInSeconds: Long,
                    initialDelay: Long,
                    interval: Long)
  extends PeriodicTaskRunner(initialDelay, interval) {

  private class MessageReceiver(commandCenter: CommandCenter) extends Actor {

    private var finishedCount = 0

    def receive() = {
      case l: LocationData => commandCenter.updateLocationData(l)
      case _ => handleFinished()
    }

    // Actor handles messages one at a time so this doesn't need
    // to be synchronized
    def handleFinished(): Unit = {
      finishedCount += 1
      if (finishedCount == commandCenter.getRoverCount()) {
        println("Shutting down Command Center")
        commandCenter.abort()
        context.system.terminate()
      }
    }
  }

  private final val initialDelayinSeconds = transmissionDelayInSeconds
  private val messageReceiver = actorSystem.actorOf(Props(new MessageReceiver(this)),
                                                    name="MessageReceiver")
  // Buffer to store the LocationData sent from the rovers
  // Assumes the receiver is faster than the sender to pick up the message; consider
  // increase the buffer size if otherwise
  private val locationDataArr = new Array[LocationData](roverCount)

  def getRoverCount(): Int = roverCount

  def getMessageReceiver() = messageReceiver

  def updateLocationData(l: LocationData): Unit = {
    locationDataArr(l.id) = l
  }

  def runTask(): Unit = {
    for(i <- 0 until roverCount) {
      val locationData = locationDataArr(i)
      if (locationData != null) {
        val sBuilder = new StringBuilder
        sBuilder.append("Rover " + locationData.id + ": ")
        sBuilder.append("Reported position " + locationData.position + ", ")
        sBuilder.append("Predicted position " + getPredictedPosition(locationData) + ", ")
        sBuilder.append("Direction " + locationData.direction + ", ")
        sBuilder.append("Speed " + locationData.speed + ", ")
        sBuilder.append("Turning angle " + locationData.turnAngle)
        println(sBuilder.toString())
      }
    }
    println()
  }

  def getPredictedPosition(l: LocationData): (Double, Double) = {
    (l.position._1 + l.speed * initialDelayinSeconds * Math.cos(l.direction),
     l.position._2 + l.speed * initialDelayinSeconds * Math.sin(l.direction))
  }
}
