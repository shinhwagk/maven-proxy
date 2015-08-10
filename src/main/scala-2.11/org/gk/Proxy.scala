package org.gk

import java.io._
import java.net.{ServerSocket, Socket}
import slick.driver.H2Driver.api._
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.config.cfg.RepoInfo
import org.gk.db.MetaData
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.Doorman.StartRepoService
import org.gk.workers._
import org.gk.workers.down.{DownWorker, DownMaster}
import slick.dbio.DBIO

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Created by gk on 15/7/21.
 */
object Proxy extends App {

  val ss = new ServerSocket(cfg.getMavenProxyPost);
  val system = ActorSystem("MavenProxy")
  val doorman = system.actorOf(Props[Doorman], name = "Doorman")

  //创建数据库表,如果没有的话
  val managerPort = cfg.getMavenPorxyManagePort
  import org.gk.db.InitDatabase._
  import org.gk.db.MetaData._

  initTable

  println("系统已经启动...")

  val centralRepoInfo = Await.result(db.run(repositoryTable.filter(_.name === "central").result), Duration.Inf).toList.head

  //启动中央仓库
  startRepoPorxy(centralRepoInfo._1, centralRepoInfo._2, centralRepoInfo._3)

  def startRepoPorxy(repoName: String, repoUrl: String, repoPort: Int): Unit = {

    val repoActorName = "RepoActorRef-" + repoName

    system.actorOf(Props[Doorman], name = repoActorName) ! StartRepoService(repoName, repoUrl, repoPort, doorman)

  }

  def startManager = {

    val ss = new ServerSocket(managerPort)
    while(true){
      val socket = ss.accept()

    }
  }
}

class ProxyCentrolAccept(socket:Socket){
  def abc: Unit ={
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val command = br.readLine()
    command match {
      case "list"=>
      case "start" =>
      case "stop" =>

    }
  }
}

object ProxyCentrolSend{
  val managerPort = cfg.getMavenPorxyManagePort
  val socket = new Socket("127.0.0.1",managerPort);
  def main (args: Array[String] ) {
    val pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
    pw.println("aaaa")
    pw.flush();
  }
}