package com.payment_typed

import akka.actor.ActorLogging
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors

object LogIncorrectPayment {

  case class Message(message: String)

  def apply(): Behavior[Message] =
    Behaviors.setup(context => new LogIncorrectPayment(context))
}

class LogIncorrectPayment(context: ActorContext[LogIncorrectPayment.Message])
  extends AbstractBehavior[LogIncorrectPayment.Message](context) {

  import LogIncorrectPayment._

  override def onMessage(msg: LogIncorrectPayment.Message): Behavior[LogIncorrectPayment.Message] = {
    Behaviors.receive { (context, command) =>
      command match {
        case Message(message) =>
          context.log.error(message)
          Behaviors.same
      }
    }
  }
}
