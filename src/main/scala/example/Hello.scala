package Continuation

import cats._
import cats.implicits._
import cats.effect.{IOApp, ExitCode, IO}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    println(ContinuationFunctions.res) // 43
    println(ContinuationFutures.res) // 0
    Thread.sleep(200)
    println(ContinuationFutures.res) // 43
    println(ComposableFunctions.value) // 43
    println(ComposableFunctions.value2) // 43
    IOContinuationMonad.value.as(ExitCode.Success)
  }

}
