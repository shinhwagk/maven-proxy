package org.gk.server

import java.net.ServerSocket
import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import org.gk.server.centrol.CommandServer
import org.gk.server.workers._
import org.gk.server.config.cfg
import org.gk.server.workers.down.DownManager


/**
 * Created by gk on 15/7/21.
 */
object ProxyServer extends App {

  val ss = new ServerSocket(cfg.getMavenProxyServicePost);

  import org.gk.server.db._

  InitDatabase.initMavenProxy
  ActorRefWokerGroups.startCommandServerActorRef
  println("系统已经启动...")

  while (true) {
    val socket = ss.accept()
    ActorRefWokerGroups.doorman ! socket
  }
}
