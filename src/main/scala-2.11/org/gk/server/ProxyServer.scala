package org.gk.server

import java.net.ServerSocket
import org.gk.server.db._
import org.gk.server.workers._
import org.gk.server.config.cfg


/**
 * Created by gk on 15/7/21.
 */
object ProxyServer extends App {

  val ss = new ServerSocket(cfg.getMavenProxyServicePost);

  import org.gk.server.db._
  import org.gk.server.workers.ActorRefWokerGroups._

  InitDatabase.initMavenProxy

  println("系统已经启动...")

  while (true) {
    val socket = ss.accept()
    ActorRefWokerGroups.doorman ! socket
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
