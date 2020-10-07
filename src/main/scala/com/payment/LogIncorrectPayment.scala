package com.payment

import akka.actor.{Actor, ActorLogging, Props}

object LogIncorrectPayment {
  sealed trait LogMessage
  case class Message(message: String) extends LogMessage

  def props(): Props = Props(new LogIncorrectPayment)
}

class LogIncorrectPayment extends Actor with ActorLogging{
  import LogIncorrectPayment._
  override def receive: Receive = {
    case Message(message) => log.error(message)
    case msg @ _ => log.warning(s"Unrecognized message: $msg")
  }
}
