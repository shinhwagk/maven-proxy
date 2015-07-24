package org.gk.httpserver

import java.io._
import java.net.{Socket, ServerSocket}
import java.util.Date

import akka.event.Logging
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
//  val listener = system.actorOf(RoundRobinPool(1).props(Props(new Listener)), name = "listener")
  val listener = system.actorOf(Props[Listener], name = "listener")
  var num = 0
  GkConsoleLogger.info("系统已经启动...")
  def main(args: Array[String]) {
    GkConsoleLogger.info("系统开始接受请求...")
    while (true) {
      val socket = ss.accept();
      num += 1
//      num += 1
//      listener ! "over"
      GkConsoleLogger.info("发送请求给requert发送者...")
      GkConsoleLogger.info("........................."+num+".....................")
      listener ! requertSocket(socket)
      GkConsoleLogger.info("........................."+num+".....................")

    }
  }
}

case class requertSocket(socket:Socket)
case class CaseResponse(path:String,socket:Socket)

class Listener extends Actor{
  val requert = context.actorOf(Props(new Requert), "Requert")
  val response = context.actorOf(Props(new Response), "Response")
  override def receive ={
    case requertSocket(socket) => {
      GkConsoleLogger.info("requert发送者接受到请求，准备处理...")
      GkConsoleLogger.info("requert发送者发出请求...")
      requert ! socket
    }
    case CaseResponse(path,socket) => {
      GkConsoleLogger.info("response接受者收到请求...")
      GkConsoleLogger.info("发送处理请求给Response...")
      response ! CaseResponse(path,socket)

    }
    case "over" =>{
      println("请求结束")
    }
  }
}

