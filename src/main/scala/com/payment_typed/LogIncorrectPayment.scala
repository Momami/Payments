package com.payment_typed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object LogIncorrectPayment {
  sealed trait LogMessage
  case class Message(message: String) extends LogMessage

  def apply(): Behavior[LogMessage] =
    Behaviors.receive { (context, message) =>
      message match {
        case Message(message) =>
          context.log.error(message)
          Behaviors.same
      }
    }
}
