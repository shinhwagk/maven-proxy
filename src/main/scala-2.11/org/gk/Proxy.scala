package org.gk

import java.net.{ServerSocket, Socket}

import akka.actor.{ActorRef, Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.config.cfg.RepoInfo
import org.gk.db.Tables._
import org.gk.workers.Doorman.StartRepoService
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

  val repoList = cfg.getRemoteRepoMap
  repoList.map(startRepoPorxy)

  def startRepoPorxy(repoInfo:Tuple2[String,RepoInfo]): Unit ={
    val repoName = repoInfo._1
    val repoUrl = repoInfo._2.url
    val repoPort = repoInfo._2.port
    val doormanActorRef = doorman

    system.actorOf(Props[Doorman]) ! StartRepoService(repoName,repoUrl,repoPort,doormanActorRef)

  }
}


