package com.payment_typed

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}
import akka.stream.Materializer

import scala.concurrent.ExecutionContext.Implicits.global

object MainClass {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new MainClass(context))

}

class MainClass(context: ActorContext[String]) extends AbstractBehavior[String](context) {

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "start" =>
        val paymentChecker = context.spawn(PaymentChecker(), "payment-checker")
        val directory = context.system.settings.config.getString("akka.reading.catalog")
        val mask = context.system.settings.config.getString("akka.reading.mask")
        val paymentsReader = new PaymentsReader(paymentChecker, directory, mask.r)(Materializer.matFromSystem(context.system.classicSystem))
        Thread.sleep(2000)
        paymentsReader.readPayments().andThen{
          case _ => context.system.terminate()
        }
        this
    }
}

object Main extends App{
  val testSystem = ActorSystem(MainClass(), "testSystem")
  testSystem ! "start"
}
