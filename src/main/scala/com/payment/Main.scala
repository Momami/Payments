package com.payment

import akka.actor.ActorSystem
import com.payment.Main.system.dispatcher

object Main extends App{
  implicit val system: ActorSystem = ActorSystem(s"Payments-system")
  val paymentChecker = system.actorOf(PaymentChecker.props())
  val directory = system.settings.config.getString("akka.reading.catalog")
  val mask = system.settings.config.getString("akka.reading.mask")
  val delimiter = system.settings.config.getString("akka.reading.delimiter")
  val maximumFrameLength = system.settings.config.getInt("akka.reading.maximumFrameLength")
  val paymentsReader = new PaymentsReader(paymentChecker, directory, mask.r, delimiter, maximumFrameLength)
  paymentsReader.readPayments().andThen{
    case _ => system.terminate()
  }
}
