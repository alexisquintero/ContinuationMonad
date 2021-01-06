package Continuation

import java.nio.channels.{ AsynchronousFileChannel, CompletionHandler }
import java.nio.file.{ Paths, StandardOpenOption }
import java.nio.ByteBuffer

object IOPain {

  val fileChannel = AsynchronousFileChannel.open(
    Paths.get(".gitignore"),
    StandardOpenOption.READ
  )

  val buffer1 = ByteBuffer.allocate(256)
  fileChannel.read(buffer1, 0, null, new CompletionHandler[Integer, Object] {

    override def failed(exc: Throwable, attachment: Object): Unit =
      println(s"Failed to read file: $exc")

    override def completed(result1: Integer, attachment: Object): Unit = {
      println(s"Read $result1 bytes")
      fileChannel.close()
      buffer1.rewind()
      buffer1.limit(result1)
      val outputFileChannel = AsynchronousFileChannel.open(
        Paths.get(".gitignorecopy"),
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE
        )
      outputFileChannel.write(buffer1, 0, null, new CompletionHandler[Integer, Object] {

        override def failed(exc: Throwable, attachment: Object): Unit =
          println(s"Failed to write file: $exc")

        override def completed(result2: Integer, attachment: Object): Unit = {
          println(s"Wrote $result2 bytes")
          outputFileChannel.close()
          val inputChannel = AsynchronousFileChannel.open(
            Paths.get(".gitignorecopy"),
            StandardOpenOption.READ
          )
          val buffer2 = ByteBuffer.allocate(256)
          inputChannel.read(buffer2, 0, null, new CompletionHandler[Integer, Object] {

            override def failed(exc: Throwable, attachment: Object): Unit =
              println(s"Failed to read new file: $exc")

            override def completed(result3: Integer, attachment: Object): Unit = {
              buffer2.rewind()
              buffer2.limit(result3)
              val isIdentical = new String(buffer2.array()) == new String(buffer1.array())
              println(s"Read $result3 bytes, contents is identical: $isIdentical")
            }

          })
        }

      })
    }

  })
}

object IOContinuationMonad {
  import cats._
  import cats.data._
  import cats.implicits._
  import cats.effect.IO

  type NioMonad[A] = ContT[IO, Unit, A]

  def nioRead(channel: AsynchronousFileChannel): NioMonad[(ByteBuffer, Integer)] = ContT[IO, Unit, (ByteBuffer, Integer)] { callback =>
    val buffer = ByteBuffer.allocate(256)
    IO.delay(
      channel.read(buffer, 0, null, new CompletionHandler[Integer, Object] {
        override def failed(exc: Throwable, attachment: Object): Unit =
          println(s"Failed to read file: $exc")
        override def completed(result: Integer, attachment: Object): Unit = {
          println(s"Cont: Read $result bytes")
          buffer.rewind()
          buffer.limit(result)
          channel.close()
          callback(buffer -> result)
        }
      })
    )
  }

  def nioWrite(buffer: ByteBuffer, channel: AsynchronousFileChannel): NioMonad[Integer] = ContT[IO, Unit, Integer] { callback =>
    IO.delay(
      channel.write(buffer, 0, null, new CompletionHandler[Integer, Object] {
        override def failed(exc: Throwable, attachment: Object): Unit =
          println(s"Failed to write file: $exc")
        override def completed(result: Integer, attachment: Object): Unit = {
          println(s"Cont: Wrote $result bytes")
          channel.close()
          callback(result)
        }
      })
    )
  }

  val channel1 = AsynchronousFileChannel.open(
    Paths.get(".gitignore"),
    StandardOpenOption.READ
  )

  val statusMonad: NioMonad[Boolean] = for {
    (buffer1a, result1a) <- nioRead(channel1)
    channel2 = AsynchronousFileChannel.open(
      Paths.get(".gitignorecopy"),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE
      )
    _ <- nioWrite(buffer1a, channel2)
    channel3 = AsynchronousFileChannel.open(
      Paths.get(".gitignorecopy"),
      StandardOpenOption.READ
      )
    (buffer2a, result3a) <- nioRead(channel3)
  } yield {
    val isIdentical = result1a == result3a &&
      new String(buffer2a.array()) == new String(buffer1a.array())
    isIdentical
  }

  val value = statusMonad.run { status =>
    IO.delay(println(s"After running the monad: Status is $status")) }

}
