package com.payment

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object PaymentParticipant {

  sealed trait PaymentCommand

  case class Payment(sign: PaymentSign, value: Long, participant: ActorRef[PaymentCommand]) extends PaymentCommand

  case class StopPayment(value: Long) extends PaymentCommand

  sealed trait PaymentSign

  final case object PlusSign extends PaymentSign

  final case object MinusSign extends PaymentSign

  def apply(name: String, balance: Long): Behavior[PaymentCommand] = {
    paymentProcess(name, balance)
  }

  def paymentProcess(name: String, balance: Long): Behavior[PaymentCommand] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Payment(MinusSign, value, participant) if value > balance =>
          context.log.error(s"User lacks balance $name! Rolling back an operation.")
          participant ! StopPayment(value)
          Behaviors.stopped
        case Payment(MinusSign, value, _) =>
          context.log.info(s"Transfer from $name: $value. Balance: ${balance - value}.")
          Behaviors.stopped
        case Payment(PlusSign, value, _) =>
          val balanceUser = balance + value
          context.log.info(s"Transfer to $name: $value. Balance: $balanceUser.")
          paymentProcess(name, balanceUser)
        case StopPayment(value) =>
          context.log.info(s"Canceling a transfer to $name: $value. Balance: ${balance - value}.")
          Behaviors.stopped
      }
    }
}
