package com.payment


import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.stream.Materializer

import scala.concurrent.ExecutionContext.Implicits.global

object OrchestratorSystem {
  def apply(): Behavior[String] =
    Behaviors.setup { context =>
      val paymentChecker = context.spawn(PaymentChecker(), "payment-checker")
      val directory = context.system.settings.config.getString("akka.reading.catalog")
      val mask = context.system.settings.config.getString("akka.reading.mask")
      val delimiter = context.system.settings.config.getString("akka.reading.delimiter")
      val maximumFrameLength = context.system.settings.config.getInt("akka.reading.maximumFrameLength")
      val paymentsReader =
        new PaymentsReader(paymentChecker, directory, mask.r, delimiter, maximumFrameLength)(
          Materializer.matFromSystem(context.system.classicSystem))
      paymentsReader.readPayments().andThen {
        case _ => context.system.terminate()
      }
      Behaviors.same
    }
}

object Main extends App {
  val mySystem = ActorSystem(OrchestratorSystem(), "PaymentsSystem")
}
