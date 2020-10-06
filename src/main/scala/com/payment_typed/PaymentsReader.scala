package com.payment_typed

import java.nio.file.{FileSystem, FileSystems}

import akka.Done
import akka.actor.typed.ActorRef
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.stream.typed.scaladsl.ActorSource
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.matching.Regex

class PaymentsReader(paymentChecker: ActorRef[PaymentChecker.CheckPayment],
                     directory: String,
                     mask: Regex)
                    (implicit val materializer: Materializer) {

  val fs: FileSystem = FileSystems.getDefault

  def readPayments(): Future[Done] =
    Directory.ls(fs.getPath(directory))
      .filter(path => mask.pattern.matcher(path.getFileName.toString).matches())
      .flatMapConcat(FileIO.fromPath(_))
      .via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = 1024))
      .map(_.utf8String)
      .runForeach(pay => paymentChecker ! PaymentChecker.CheckPayment(pay))

  def read() = {
    val source: Source[String, ActorRef[String]] = ActorSource.actorRef[String](completionMatcher = {
      case _: String =>
    }, failureMatcher = {
      case _ => "Error"
    }, bufferSize = 8, overflowStrategy = OverflowStrategy.fail)

    val ref = source
      .collect {
        case Message(msg) => msg
      }
      .to(Sink.foreach(println))
      .run()
  }
}
