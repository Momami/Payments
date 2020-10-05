package com.payment

import akka.actor.ActorSystem
import com.payment.Main.system.dispatcher

object Main extends App{
  implicit val system: ActorSystem = ActorSystem(s"Payments-system")
  val paymentChecker = system.actorOf(PaymentChecker.props())
  val directory = system.settings.config.getString("akka.reading.catalog")
  val mask = system.settings.config.getString("akka.reading.mask")
  val paymentsReader = new PaymentsReader(paymentChecker, directory, mask.r)
  paymentsReader.readPayments().andThen{
    case _ => system.terminate()
  }
}
