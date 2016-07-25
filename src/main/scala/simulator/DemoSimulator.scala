package simulator

import java.io.{PrintWriter, FileOutputStream, File}
import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import datagenerator.{LocationData, LocationDataBuilder, SimpleRNG}
import model.{MoonRover, CommandCenter}

import scala.collection.mutable
import scala.io.Source

class DemoSimulator() {

  val conf = ConfigFactory.load()
  val fileName = conf.getString("data_file_path")
  val roverCount = conf.getInt("rover.count")
  val durationInMinutes = conf.getInt("simulation_duration_in_minutes")

  def genTestFile(): Unit = {
    val writer = new PrintWriter(new FileOutputStream(new File(fileName), false))
    val dataBuilder = new LocationDataBuilder(
                            (conf.getDouble("rover.init_position.x"),
                             conf.getDouble("rover.init_position.y")),
                            conf.getInt("rover.init_turn_angle"),
                            conf.getInt("rover.speed_limit"),
                            durationInMinutes * 60)
    val sequenceArr = new Array[List[LocationData]](roverCount)
    for (i <- 0 until roverCount) {
      val rng = SimpleRNG(i)
      sequenceArr(i) = dataBuilder.buildDataSequence(i, rng)
    }
    for (i <- sequenceArr(0).indices)
      for (j <- 0 until roverCount) {
        val locationData = sequenceArr(j)(i)
        writer.println(locationData.serialize())
      }
    writer.close()
  }

  def runDemo(): Unit = {
    val sequenceArr = new Array[mutable.Builder[LocationData, Vector[LocationData]]](roverCount)
    for (i <- 0 until roverCount) {
      sequenceArr(i) = Vector.newBuilder[LocationData]
    }
    for (line <- Source.fromFile(fileName).getLines()) {
      val locationData = LocationData.deserialize(line)
      sequenceArr(locationData.id) += locationData
    }
    val commandCenter = new CommandCenter(roverCount,
                              conf.getDuration("rover.transmission_delay", TimeUnit.SECONDS),
                              0, // no delay
                              conf.getDuration("command_center.interval", TimeUnit.MILLISECONDS))
    commandCenter.start()

    val roverArr = new Array[MoonRover](roverCount)
    for (i <- 0 until roverCount) {
      val rover = new MoonRover(i,
                                sequenceArr(i).result(),
                                commandCenter,
                                conf.getDuration("rover.transmission_delay", TimeUnit.MILLISECONDS),
                                conf.getDuration("rover.interval", TimeUnit.MILLISECONDS))
      roverArr(i) = rover
      rover.start()
    }
    commandCenter.join()
    roverArr.foreach(r => r.shutdown())
  }
}

object DemoSimulator {

  def main(args: Array[String]): Unit = {
    val demo = new DemoSimulator()
    demo.genTestFile()
    demo.runDemo()
  }
}
