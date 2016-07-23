package datagenerator

trait RNG {
  def nextInt: (Int, RNG)
}

case class SimpleRNG(seed: Long) extends RNG {
  def nextInt: (Int, RNG) = {
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL
    val nextRNG = SimpleRNG(newSeed)
    val n = (newSeed >>> 16).toInt
    (n, nextRNG)
  }

}

/**
  * A random number generator with initial states
  * By propagating states, this RNG can output pseudo random numbers
  * deterministically, which is easier for testing purpose
  *
  * Reference: Chapter 6, Functional Programming in Scala by P. Chiusano et al
  *
  */
object RandomNumberGenerator {

  import State._

  case class State[S, +A](run: S => (A, S)) {

    def flatMap[B](f: A => State[S, B]): State[S, B] = {
      State(s => {
        val (a, ns) = run(s)
        f(a).run(ns)
      })
    }

    def map[B](f: A => B): State[S, B] =
      flatMap[B](a => unit(f(a)))

    def map2[B, C](sb: State[S, B])(f: (A, B) => C): State[S, C] =
      flatMap(a => sb.map(b => f(a, b)))

    def get[S]: State[S, S] = State(s => (s, s))

    def set[S](s: S): State[S, Unit] = State(_ => ((), s))
  }

  object State {
    def unit[S, A](a: A): State[S, A] = State(s => (a, s))

    def sequence[S, A](fs: List[State[S, A]]): State[S, List[A]] = {
      fs.foldRight(unit[S, List[A]](List[A]()))((s, l) => s.map2(l)(_ :: _))
    }
  }

  type SRand[A] = State[RNG, A]

  def int: SRand[Int] = State(_.nextInt)

  def ints(count: Int): SRand[List[Int]] =
    sequence(List.fill(count)(int))

  def double: SRand[Double] = nonNegativeInt.map(_ / (Int.MaxValue.toDouble + 1))

  def doubles(count: Int): SRand[List[Double]] =
    sequence(List.fill(count)(double))

  def nonNegativeInts(count: Int): SRand[List[Int]] =
    sequence(List.fill(count)(nonNegativeInt))

  def nonNegativeInt: SRand[Int] = {
    int.map { n =>
      if (n < 0) -(n + 1) else n
    }
  }

  def nonNegativeLessThan(n: Int): SRand[Int] = {
    nonNegativeInt.flatMap { i =>
      val mod = i % n
      if (i + (n - 1) - mod >= 0) unit(mod) else nonNegativeLessThan(n)
    }
  }

  def nonNegativeLessThanList(n: Int, count: Int): SRand[List[Int]] =
    sequence(List.fill(count)(nonNegativeLessThan(n)))
}
