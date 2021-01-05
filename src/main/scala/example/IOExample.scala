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
        Paths.get(".gitignore-copy"),
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
            Paths.get(".gitignore-copy"),
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

  type NioMonad[A] = ContT[Id, Unit, A]

  def nioRead(channel: AsynchronousFileChannel): NioMonad[(ByteBuffer, Integer)] = ContT[Id, Unit, (ByteBuffer, Integer)] {
    callback =>
      val buffer = ByteBuffer.allocate(256)
      channel.read(buffer, 0, null, new CompletionHandler[Integer, Object] {
        override def failed(exc: Throwable, attachment: Object): Unit =
          println(s"Failed to read file: $exc")
        override def completed(result: Integer, attachment: Object): Unit = {
          println(s"Cont: Read $result bytes")
          buffer.rewind()
          buffer.limit(result)
          channel.close()
          callback((buffer, result))
        }
      })
  }
}
