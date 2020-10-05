package com.payment

import akka.actor.{Actor, ActorLogging, Props}

object LogIncorrectPayment {
  case class Message(message: String)

  def props(): Props = Props(new LogIncorrectPayment)
}

class LogIncorrectPayment extends Actor with ActorLogging{
  import LogIncorrectPayment._
  override def receive: Receive = {
    case Message(message) => log.error(message)
  }
}
