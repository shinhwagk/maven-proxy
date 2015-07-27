package org.gk.workers

import java.net.{ServerSocket, Socket}

import akka.actor.{ActorRef, Props, Actor}
import org.gk.log.GkConsoleLogger

/**
 * Created by goku on 2015/7/24.
 */
object Doorman{
  def props(socket:Socket):Props = Props(new Doorman(socket))
}
class Doorman(socket:Socket) extends Actor{
  val terminator = context.actorOf(Props[Terminator])
//  val requert = context.actorOf(Requert.props(socket), "Requert")
//  val response = context.actorOf(Response.props(socket), "Response")
  override def receive: Receive = {
    case socket =>{
      GkConsoleLogger.info("requert发送者接受到请求，准备处理...")
      GkConsoleLogger.info("requert发送者发出请求...")
//      requert ! "requert"

    }
  }
}
