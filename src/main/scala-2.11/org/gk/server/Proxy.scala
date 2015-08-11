package org.gk.server

import java.net.ServerSocket

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.gk.server.centrol.CommandServer
import org.gk.server.config.cfg
import org.gk.server.workers.Doorman


/**
 * Created by gk on 15/7/21.
 */
object Proxy extends App {

  val ss = new ServerSocket(cfg.getMavenProxyServicePost);
  val system = ActorSystem("MavenProxy", ConfigFactory.load("server"))
  val doorman = system.actorOf(Props[Doorman], name = "Doorman")
  system.actorOf(Props[CommandServer], name = "CommandServer")

  import org.gk.server.db._

  InitDatabase.initMavenProxy

  println("系统已经启动...")

  while (true) {
    val socket = ss.accept()
    doorman ! socket
  }

  //  val centralRepoInfo = Await.result(db.run(repositoryTable.filter(_.name === "central").result), Duration.Inf).toList.head

  //启动中央仓库
  //  startRepoPorxy(centralRepoInfo._1, centralRepoInfo._2, centralRepoInfo._3)

//  def startRepoPorxy(repoName: String, repoUrl: String, repoPort: Int): Unit = {
//
//    val repoActorName = "RepoActorRef-" + repoName
//
//    //    system.actorOf(Props[Doorman], name = repoActorName) ! StartRepoService(repoName, repoUrl, repoPort, doorman)
//
//  }
}
