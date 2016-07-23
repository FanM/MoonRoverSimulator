package simulator

import java.io.File

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.{BePropertyMatcher, BePropertyMatchResult}
import test.UnitTestBase

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class DemoSimulatorTest extends UnitTestBase {

  val conf = ConfigFactory.load()
  val fileName = conf.getString("data_file_path")
  val roverCount = conf.getInt("rover.count")
  val durationInMinutes = conf.getInt("simulation_duration_in_minutes")

  class FileBePropertyMatcher extends BePropertyMatcher[java.io.File] {
    def apply(left: File) =
      BePropertyMatchResult(left.isFile, "file")
  }

  "Generate the test file" should " succeed" in {
    val demo = new DemoSimulator()
    demo.genTestFile()
    val file = new FileBePropertyMatcher
    val temp = new File(fileName)
    temp should be a file
    Source.fromFile(fileName).getLines().size should be
      roverCount * durationInMinutes * 60
  }

  "Run simulator for 1 minute " should " succeed" in {
    val demo = new DemoSimulator()
    demo.genTestFile()
    demo.runDemo()
  }

}
