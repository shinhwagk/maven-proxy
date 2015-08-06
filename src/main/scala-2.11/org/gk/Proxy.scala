package org.gk

import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorSystem, Props}
import org.gk.config.cfg
import org.gk.db.Tables._
import org.gk.workers._
import org.gk.workers.down.{DownWorker, DownMaster}


/**
 * Created by gk on 15/7/21.
 */
object Proxy extends App {
  val ss = new ServerSocket(cfg.getMavenProxyPost);
  val system = ActorSystem("MavenProxy")
  val doorman = system.actorOf(Props[Doorman], name = "Doorman")

  //创建数据库表,如果没有的话
  import org.gk.db.InitDatabase._
  initTable

  println("系统已经启动...")

  while (true) {
    println("系统开始接受请求...")
    val socket = ss.accept();

    doorman ! socket
    println("发送请求给doorman...")
  }
}


case class requertSocket(socket:Socket)
case class CaseResponse(path:String,socket:Socket)

