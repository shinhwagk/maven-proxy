package org.gk

import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}
import org.gk.config.cfg
import org.gk.workers._



/**
 * Created by gk on 15/7/21.
 */
object Proxy extends {
  val ss = new ServerSocket(cfg.getMavenProxyPost);
  val system = ActorSystem("MavenProxy")
  val headParser = system.actorOf(Props[HeadParser], name = "HeadParser")

  import org.gk.db.InitDatabase._
  initTable

  println("系统已经启动...")

  def main(args: Array[String]) {
    println("系统开始接受请求...")

    while (true) {
      val socket = ss.accept();
      headParser ! socket
      println("发送请求给headParser...")
    }
  }
}


case class requertSocket(socket:Socket)
case class CaseResponse(path:String,socket:Socket)

