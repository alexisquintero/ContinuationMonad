package Continuation

object ComposableFunctions {
  def add3(x: Int)(k: Int => Unit): Unit = k(x + 3)
  def mul4(x: Int)(k: Int => Unit): Unit = k(x * 4)
  def const(x: Int)(k: Int => Unit): Unit = k(x)

  val c10: (Int => Unit) => Unit = const(10)

  type RC[A] = (A => Unit) => Unit

  val step1: Int => RC[Int] = (x: Int) => mul4(x)
  val c40: RC[Int] = const(40)

  val step2: Int => RC[Int] = (x: Int) => add3(x)
  val c43: RC[Int] = const(43)

  implicit class C1[A](rc: RC[A]) {
    def @@[B](f: A => RC[B]): RC[B] = { (cb: B => Unit) =>
      rc(a => f(a)(cb))
    }
  }

  val result: RC[Int] = c10 @@ step1 @@ step2

  def extractValue(rc: RC[Int]): Int = {
    var result = 0
    rc(result = _)
    result
  }

  val value: Int = extractValue(result)

  def pure[A](a: A): RC[A] = { (ca: A => Unit) => ca(a) }

  implicit class C2[A](rc: RC[A]) {
    def map[B](f: A => B): RC[B] = { (cb: B => Unit) =>
      // rc(a => cb(f(a)))
      rc(cb compose f)
    }

    def flatMap[B](f: A => RC[B]): RC[B] = { (cb: B => Unit) =>
      rc(a => f(a)(cb))
    }
  }

  val result2: RC[Int] = for {
    x <- pure(10)
    y <- mul4(x) _
    z <- add3(y) _
  } yield z

  val value2:Int = extractValue(result2)
}
