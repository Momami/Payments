package com.payment

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object PaymentParticipant {
  case class Payment(sign: PaymentSign, value: Long, participant: ActorRef)
  trait PaymentSign
  case object PlusSign extends PaymentSign
  case object MinusSign extends PaymentSign
  case class StopPayment(value: Long)

  def props(name: String, balance: Long): Props = Props(new PaymentParticipant(name, balance))
}

class PaymentParticipant(name: String, var balance: Long) extends Actor with ActorLogging{
  import PaymentParticipant._

  override def receive: Receive = {
    case Payment(MinusSign, value, participant) if value > balance =>
      log.error(s"User lacks balance $name! Rolling back an operation.")
      participant ! StopPayment(value)
    case Payment(MinusSign, value, participant) =>
      balance -= value
      log.info(s"Transfer user $participant from $name: $value. Balance: $balance.")
    case Payment(PlusSign, value, participant) =>
      balance += value
      log.info(s"Transfer user $name from $participant: $value. Balance: $balance.")
    case StopPayment(value) =>
      balance -= value
      log.info(s"Canceling a transfer to a user ${sender()} from $name: $value. Balance: $balance.")
  }

}
