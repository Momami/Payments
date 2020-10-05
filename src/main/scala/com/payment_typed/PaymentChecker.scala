package com.payment_typed

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors

import scala.util.matching.Regex

object PaymentChecker {
  case class CheckPayment(payment: String)

  def apply(): Behavior[CheckPayment] =
    Behaviors.setup(context => new PaymentChecker(context))
}

class PaymentChecker(context: ActorContext[PaymentChecker.CheckPayment])
  extends AbstractBehavior[PaymentChecker.CheckPayment](context) {

  import PaymentChecker._

  val numChar = "[\\wа-яА-Я]+"
  val mask: Regex = s"($numChar) -> ($numChar): (\\d+)".r


  val balance: Long = context.system.settings.config.getLong("akka.actor.balance")
  val logger: ActorRef[LogIncorrectPayment.Message] = context.spawn(LogIncorrectPayment(), "logger")


  override def onMessage(message: CheckPayment): Behavior[CheckPayment] = {
    Behaviors.receiveMessage {
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

  protected def createPaymentParticipant(name: String): ActorRef[PaymentParticipant.PaymentCommand] = {
    context.spawn(PaymentParticipant(name, balance), s"paymentParticipant_$name")
  }
}