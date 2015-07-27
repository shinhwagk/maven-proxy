package org.gk

import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}
import org.gk.config.cfg
import org.gk.log.GkConsoleLogger
import org.gk.workers._


/**
 * Created by gk on 15/7/21.
 */
object Proxy {
  val ss = new ServerSocket(cfg.getMavenProxyPost);
  val system = ActorSystem("MavenProxy")
  val headParser = system.actorOf(Props[HeadParser], name = "HeadParser")
//  val listener = system.actorOf(RoundRobinPool(1).props(Props(new Listener)), name = "listener")


  GkConsoleLogger.info("系统已经启动...")

  def main(args: Array[String]) {
    GkConsoleLogger.info("系统开始接受请求...")


    while (true) {
      val socket = ss.accept();
      headParser ! socket
      GkConsoleLogger.info("发送请求给headParser...")
//      GkConsoleLogger.info("........................."+num+".....................")
    }
  }
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
      println("abc.............................")
//      println(context.self.path.name)
    }
  }
}

