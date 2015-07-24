package org.gk.httpserver

import java.io._
import java.net.{Socket, ServerSocket}
import java.util.Date

import akka.actor.Actor.Receive
import akka.event.Logging
import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.httpserver.service.maven.{Doorman, Requert, Response}
import org.gk.log.GkConsoleLogger


/**
 * Created by gk on 15/7/21.
 */
object HttpServer {

  val ss = new ServerSocket(cfg.getMPPort);
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
      val doorman = system.actorOf(Doorman.props(listener,socket), name ="Doorman"+num)
      doorman ! "requert"
      GkConsoleLogger.info("发送请求给requert发送者...")
      GkConsoleLogger.info("........................."+num+".....................")


    }
  }
}

object head {

}
case class requertSocket(socket:Socket)
case class CaseResponse(path:String,socket:Socket)


class Listener extends Actor{

  override def receive ={
    case requertSocket(socket) => {

//      requert ! socket
    }
    case CaseResponse(path,socket) => {
      GkConsoleLogger.info("response接受者收到请求...")
      GkConsoleLogger.info("发送处理请求给Response...")
//      response ! CaseResponse(path,socket)

    }
    case "over" =>{
      println("请求结束")
    }
    case "a"=>{
      println("abc")
      context.stop(self)
    }
  }
}

