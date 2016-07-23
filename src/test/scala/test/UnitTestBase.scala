package test

import org.scalatest.{FlatSpec, Matchers}

trait UnitTestBase extends FlatSpec with TestBase

trait TestBase extends Matchers
