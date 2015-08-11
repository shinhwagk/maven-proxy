package org.gk.server

import java.io._
import java.net.{ServerSocket, Socket}
import com.typesafe.config.ConfigFactory
import org.gk.server.centrol.CommandServer
import org.gk.server.config.cfg
import org.gk.server.db.{MetaData, InitDatabase}
import org.gk.server.workers.Doorman
import slick.driver.H2Driver.api._
import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import slick.dbio.DBIO

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Created by gk on 15/7/21.
 */
object Proxy extends App {

  val ss = new ServerSocket(cfg.getMavenProxyPost);
  val system = ActorSystem("MavenProxy",ConfigFactory.load("server"))
  val doorman = system.actorOf(Props[Doorman], name = "Doorman")
  system.actorOf(Props[CommandServer], name = "CommandServer")

  //创建数据库表,如果没有的话
  val managerPort = cfg.getMavenPorxyManagePort
  import InitDatabase._
  import MetaData._

  initTable

  println("系统已经启动...")

//  val centralRepoInfo = Await.result(db.run(repositoryTable.filter(_.name === "central").result), Duration.Inf).toList.head

  //启动中央仓库
//  startRepoPorxy(centralRepoInfo._1, centralRepoInfo._2, centralRepoInfo._3)

  def startRepoPorxy(repoName: String, repoUrl: String, repoPort: Int): Unit = {

    val repoActorName = "RepoActorRef-" + repoName

//    system.actorOf(Props[Doorman], name = repoActorName) ! StartRepoService(repoName, repoUrl, repoPort, doorman)

  }

  def startManager = {

    val ss = new ServerSocket(managerPort)
    while(true){
      val socket = ss.accept()

    }
  }
}
