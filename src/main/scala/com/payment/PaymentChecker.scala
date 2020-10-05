package com.payment

import akka.actor.{Actor, ActorRef, Props}

import scala.util.matching.Regex

object PaymentChecker {
  case class CheckPayment(payment: String)

  def props(): Props = Props(new PaymentChecker())
}

class PaymentChecker() extends Actor{
  import PaymentChecker._

  val numChar = "[\\wа-яА-Я]+"
  val mask: Regex = s"($numChar) -> ($numChar): (\\d+)".r


  val balance: Long = context.system.settings.config.getLong("akka.actor.balance")
  val logger = context.actorOf(LogIncorrectPayment.props())

  override def receive: Receive = {
    case CheckPayment(mask(name1, name2, value)) =>
      val participant1 = createPaymentParticipant(name1)
      val participant2 = createPaymentParticipant(name2)
      participant1 ! PaymentParticipant.Payment(PaymentParticipant.MinusSign, value.toLong, participant2)
      participant2 ! PaymentParticipant.Payment(PaymentParticipant.PlusSign, value.toLong, participant1)
    case CheckPayment(payment) => logger ! LogIncorrectPayment.Message(s"Incorrect transfer: $payment.")
  }

  protected def createPaymentParticipant(name: String): ActorRef =
    context.actorOf(PaymentParticipant.props(name, balance))
}
