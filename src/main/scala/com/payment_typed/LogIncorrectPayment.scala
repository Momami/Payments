package com.payment_typed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object LogIncorrectPayment {

  case class Message(message: String)

  def apply(): Behavior[Message] =
    Behaviors.receive { (context, message) =>
      message match {
        case Message(message) =>
          context.log.error(message)
          Behaviors.same
      }
    }
}
