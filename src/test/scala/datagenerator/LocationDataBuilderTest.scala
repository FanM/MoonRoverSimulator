package datagenerator

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import test.UnitTestBase

@RunWith(classOf[JUnitRunner])
class LocationDataBuilderTest extends UnitTestBase {

  "Turing angles " should " be calculated as counterclockwise" in {
    val dataBuilder = new LocationDataBuilder((0,0), 0, 5, 1)
    dataBuilder.getDirection(0, 2 * Math.PI) should be (0)
    dataBuilder.getDirection(Math.PI / 4, - Math.PI / 4) should be (Math.PI / 2)
    dataBuilder.getDirection(0, Math.PI / 4) should be (Math.PI / 4 * 7)
  }

  "Build 10 seconds simulation data" should "succeed" in {
    val rng = SimpleRNG(-20)
    val dataBuilder = new LocationDataBuilder((0,0), 0, 5, 10)
    val dataSequence = dataBuilder.buildDataSequence(0, rng)
    dataSequence should have size 10

    dataSequence.forall(l => {
      l.direction >= 0 && l.direction < Math.PI * 2
    })
  }
}
