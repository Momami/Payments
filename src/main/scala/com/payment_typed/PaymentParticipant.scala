package com.payment_typed

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object PaymentParticipant {

  trait PaymentCommand

  case class Payment(sign: PaymentSign, value: Long, participant: ActorRef[PaymentCommand]) extends PaymentCommand

  case class StopPayment(value: Long) extends PaymentCommand

  trait PaymentSign

  final case object PlusSign extends PaymentSign

  final case object MinusSign extends PaymentSign

  def apply(name: String, balance: Long): Behavior[PaymentCommand] =
    Behaviors.setup(context => new PaymentParticipant(context, name, balance))
}

class PaymentParticipant(context: ActorContext[PaymentParticipant.PaymentCommand],
                         name: String,
                         var balance: Long)
  extends AbstractBehavior[PaymentParticipant.PaymentCommand](context) {

  import PaymentParticipant._

  override def onMessage(msg: PaymentParticipant.PaymentCommand): Behavior[PaymentParticipant.PaymentCommand] = {
    msg match {
      case Payment(MinusSign, value, participant) if value > balance =>
        context.log.error(s"User lacks balance $name! Rolling back an operation.")
        participant ! StopPayment(value)
        Behaviors.same
      case Payment(MinusSign, value, _) =>
        balance -= value
        context.log.info(s"Transfer from $name: $value. Balance: $balance.")
        Behaviors.same
      case Payment(PlusSign, value, _) =>
        balance += value
        context.log.info(s"Transfer to $name: $value. Balance: $balance.")
        Behaviors.same
      case StopPayment(value) =>
        balance -= value
        context.log.info(s"Canceling a transfer to $name: $value. Balance: $balance.")
        Behaviors.same
    }
  }
}
