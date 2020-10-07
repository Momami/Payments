package com.payment_typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object PaymentParticipant {

  sealed trait PaymentCommand
  case class Payment(sign: PaymentSign, value: Long, participant: ActorRef[PaymentCommand]) extends PaymentCommand
  case class StopPayment(value: Long) extends PaymentCommand

  sealed trait PaymentSign
  final case object PlusSign extends PaymentSign
  final case object MinusSign extends PaymentSign

  def apply(name: String, balance: Long): Behavior[PaymentCommand] =
    Behaviors.setup { context =>
      var balanceUser = balance
      Behaviors.receiveMessage{
        case Payment(MinusSign, value, participant) if value > balanceUser =>
          context.log.error(s"User lacks balance $name! Rolling back an operation.")
          participant ! StopPayment(value)
          Behaviors.same
        case Payment(MinusSign, value, _) =>
          balanceUser -= value
          context.log.info(s"Transfer from $name: $value. Balance: $balanceUser.")
          Behaviors.stopped
        case Payment(PlusSign, value, _) =>
          balanceUser += value
          context.log.info(s"Transfer to $name: $value. Balance: $balanceUser.")
          Behaviors.same
        case StopPayment(value) =>
          balanceUser -= value
          context.log.info(s"Canceling a transfer to $name: $value. Balance: $balanceUser.")
          Behaviors.stopped
      }
    }
}
