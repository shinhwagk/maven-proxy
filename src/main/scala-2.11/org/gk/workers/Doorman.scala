package org.gk.workers

import java.net.{ServerSocket, Socket}

import akka.actor.{ActorRef, Props, Actor}

/**
 * Created by goku on 2015/7/24.
 */
object Doorman{
  def props(socket:Socket):Props = Props(new Doorman(socket))
}
class Doorman(socket:Socket) extends Actor{
  val terminator = context.actorOf(Props[Terminator])
  override def receive: Receive = {
    case socket =>{
      println("requert发送者接受到请求，准备处理...")
      println("requert发送者发出请求...")

    }
  }
}
