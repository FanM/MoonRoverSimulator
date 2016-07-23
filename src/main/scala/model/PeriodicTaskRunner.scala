package model

import java.util.concurrent.TimeUnit

import akka.actor.{Cancellable, ActorSystem}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Schedules periodical tasks at intervalInMillisecond
  * with initial delay at initialDelayInMillisecond
  *
  */
abstract class PeriodicTaskRunner(val initialDelayInMillisecond: Long,
                                  val intervalInMillisecond: Long) {

  protected val actorSystem = ActorSystem()
  private val scheduler = actorSystem.scheduler
  private var currentTask: Cancellable = null

  private implicit val executor = actorSystem.dispatcher

  def runTask(): Unit

  def start(): Unit = {
    val runnable = new Runnable {
      override def run(): Unit = runTask()
    }

    currentTask = scheduler.schedule(
      Duration(initialDelayInMillisecond, TimeUnit.MILLISECONDS),
      Duration(intervalInMillisecond, TimeUnit.MILLISECONDS),
      runnable
    )
  }

  def join(): Unit = {
    Await.ready(actorSystem.whenTerminated, Duration.Inf)
  }

  def abort(): Unit = {
    currentTask.cancel()
  }

  def shutdown(): Unit = {
    actorSystem.terminate()
  }

}
