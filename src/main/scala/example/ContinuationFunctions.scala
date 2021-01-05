package Continuation

object ContinuationFunctions {

  def add3(x: Int)(k: Int => Unit): Unit = {
    val result = x + 3
    k(result)
    ()
  }

  def mul4(x: Int)(k: Int => Unit): Unit = {
    val result = x * 4
    k(result)
    ()
  }

  def const(x: Int)(k: Int => Unit): Unit = {
    val result = x
    k(result)
    ()
  }

  var res = 0

  const(10) { r1 =>
    mul4(r1) { r2 =>
      add3(r2) { r3 =>
        res = r3
      }
    }
  }

}

object ContinuationFutures {
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global

  def add3(x: Int)(k: Int => Unit): Unit = {
    Future {
      val result = x + 3
      k(result)
    }
  ()
  }

  def mul4(x: Int)(k: Int => Unit): Unit = {
    Future {
      val result = x * 4
      k(result)
    }
  ()
  }

  def const(x: Int)(k: Int => Unit): Unit = {
    Future {
      val result = x
      k(result)
    }
  ()
  }

  var res = 0

  const(10) { r1 =>
    mul4(r1) { r2 =>
      add3(r2) { r3 =>
        res = r3
      }
    }
  }

}
