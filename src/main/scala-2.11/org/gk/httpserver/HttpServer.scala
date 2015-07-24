package org.gk.httpserver

import java.io._
import java.net.{Socket, ServerSocket}
import java.util.Date

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.routing.RoundRobinPool
import org.gk.httpserver.service.maven.{Requert, Response}
import org.gk.log.GkConsoleLogger


/**
 * Created by gk on 15/7/21.
 */
object HttpServer {

  val ss = new ServerSocket(8082);
  val system = ActorSystem("MavenProxy")
  val listener = system.actorOf(RoundRobinPool(2).props(Props(new Listener)), name = "listener")
  var num = 0
  GkConsoleLogger.info("系统已经启动...")
  def main(args: Array[String]) {
    GkConsoleLogger.info("准备接受请求...")
    while (true) {
      val socket = ss.accept();
//      num += 1
      listener ! "over"
//      listener ! socket
    }
  }
}

case class requertSocket(socket:Socket)
case class ResponseSocket(socket:Socket)

class Listener extends Actor{
  val requert = context.actorOf(Props(new Requert), "Requert")
  val response = context.actorOf(Props(new Response), "Response")
  override def receive ={
    case requertSocket(socket) => {
      println("abccc")
      requert ! socket
    }
    case ResponseSocket(socket) => {
      GkConsoleLogger.info("发送处理请求给Response...")
    }
    case "over" =>{
      println("abcggggggggggg111111")
    }
  }
}

