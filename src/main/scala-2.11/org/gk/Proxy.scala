package org.gk

import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}
import org.gk.config.cfg
import org.gk.workers._
import org.gk.workers.down.DownMaster


/**
 * Created by gk on 15/7/21.
 */
object Proxy extends {
  val ss = new ServerSocket(cfg.getMavenProxyPost);
  val system = ActorSystem("MavenProxy")
  val headParser = system.actorOf(Props[HeadParser], name = "HeadParser")
  val downMaster = system.actorOf(Props[DownMaster], name = "downMaster")

  //创建数据库表,如果没有的话
  import org.gk.db.InitDatabase._
  initTable

  //下载未完成的work



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

