package com.payment


import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.util.matching.Regex

object PaymentChecker {
  sealed trait CheckOperation
  case class CheckPayment(payment: String) extends CheckOperation

  def apply(): Behavior[CheckOperation] =
    Behaviors.setup { context =>

      val mask: Regex = context.system.settings.config.getString("akka.actor.checker.mask").r
      val balance: Long = context.system.settings.config.getLong("akka.actor.participant.balance")
      val logger: ActorRef[LogIncorrectPayment.Message] = context.spawn(LogIncorrectPayment(), "logger")

      def createPaymentParticipant(name: String): ActorRef[PaymentParticipant.PaymentCommand] =
        context.spawn(PaymentParticipant(name, balance), s"paymentParticipant_$name")

      Behaviors.receiveMessage{
        case CheckPayment(mask(name1, name2, value)) =>
          val participant1 = createPaymentParticipant(name1)
          val participant2 = createPaymentParticipant(name2)
          participant1 ! PaymentParticipant.Payment(PaymentParticipant.MinusSign, value.toLong, participant2)
          participant2 ! PaymentParticipant.Payment(PaymentParticipant.PlusSign, value.toLong, participant1)
          Behaviors.same
        case CheckPayment(payment) =>
          logger ! LogIncorrectPayment.Message(s"Incorrect transfer: $payment.")
          Behaviors.same
      }
    }
}
