package com.payment

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object PaymentParticipant {
  sealed trait Operation
  case class Payment(sign: PaymentSign, value: Long, participant: ActorRef) extends Operation
  sealed trait PaymentSign
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
    case Payment(MinusSign, value, _) =>
      balance -= value
      log.info(s"Transfer from $name: $value. Balance: $balance.")
    case Payment(PlusSign, value, _) =>
      balance += value
      log.info(s"Transfer to $name: $value. Balance: $balance.")
    case StopPayment(value) =>
      balance -= value
      log.info(s"Canceling a transfer to $name: $value. Balance: $balance.")
    case msg @ _ => log.warning(s"Unrecognized message: $msg")
  }

}
