package com.payment

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.util.matching.Regex

object PaymentChecker {
  sealed trait CheckOperation
  case class CheckPayment(payment: String) extends CheckOperation

  def props(): Props = Props(new PaymentChecker())
}

class PaymentChecker() extends Actor with ActorLogging {
  import PaymentChecker._

  val mask: Regex = context.system.settings.config.getString("akka.actor.checker.mask").r
  val balance: Long = context.system.settings.config.getLong("akka.actor.participant.balance")
  val logger: ActorRef = context.actorOf(LogIncorrectPayment.props())

  override def receive: Receive = {
    case CheckPayment(mask(name1, name2, value)) =>
      val participant1 = createPaymentParticipant(name1)
      val participant2 = createPaymentParticipant(name2)
      participant1 ! PaymentParticipant.Payment(PaymentParticipant.MinusSign, value.toLong, participant2)
      participant2 ! PaymentParticipant.Payment(PaymentParticipant.PlusSign, value.toLong, participant1)
    case CheckPayment(payment) => logger ! LogIncorrectPayment.Message(s"Incorrect transfer: $payment.")
    case msg @ _ => log.warning(s"Unrecognized message: $msg")
  }

  protected def createPaymentParticipant(name: String): ActorRef =
    context.actorOf(PaymentParticipant.props(name, balance))
}
