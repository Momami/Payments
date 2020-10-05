package com.payment

import java.nio.file.{FileSystem, FileSystems}

import akka.Done
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.stream.alpakka.file.scaladsl.Directory
import akka.stream.scaladsl.{FileIO, Framing, Sink}
import akka.util.ByteString
import com.payment.Main.system.dispatcher

import scala.concurrent.Future
import scala.util.matching.Regex

class PaymentsReader(paymentChecker: ActorRef,
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
      .runForeach(pay => paymentChecker ! PaymentChecker.CheckPayment(pay)).andThen {
        case _ =>
      }
}

