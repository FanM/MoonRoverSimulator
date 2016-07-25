package datagenerator

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import test.UnitTestBase


@RunWith(classOf[JUnitRunner])
class RandomNumberGeneratorTest extends UnitTestBase {

  "nonNegativeLessThan " should "return non-negative integer less than the given number" in {
    val rng = SimpleRNG(-2020)
    RandomNumberGenerator.nonNegativeLessThan(10).run(rng)._1 should (be >=0 and be < 10)
  }

  "doubles " should "return non-negative doubles less than 1" in {
    val rng = SimpleRNG(0)

    RandomNumberGenerator.doubles(10).run(rng)._1.forall(d => d >= 0 && d < 1) should be (true)
  }
}
