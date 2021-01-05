package Continuation

object Main extends App {
  println(ContinuationFunctions.res) // 43
  println(ContinuationFutures.res) // 0
  Thread.sleep(200)
  println(ContinuationFutures.res) // 43
  println(ComposableFunctions.value) // 43
  println(ComposableFunctions.value2) // 43
}
