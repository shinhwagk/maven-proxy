package org.gk.httpserver.service.maven

import java.net.{ServerSocket, Socket}

import akka.actor.{ActorRef, Props, Actor}
import org.gk.log.GkConsoleLogger

/**
 * Created by goku on 2015/7/24.
 */
object Doorman{
  def props(listener:ActorRef,socket:Socket):Props = Props(new Doorman(listener,socket))
}
class Doorman(listener:ActorRef,socket:Socket) extends Actor{

  val requert = context.actorOf(Requert.props(socket), "Requert")
  val response = context.actorOf(Response.props(socket), "Response")
  override def receive: Receive = {
    case "requert" =>{
      GkConsoleLogger.info("requert发送者接受到请求，准备处理...")
      GkConsoleLogger.info("requert发送者发出请求...")
      listener ! "a"
      socket.close()
//      context.stop(self)
    }
  }
}
