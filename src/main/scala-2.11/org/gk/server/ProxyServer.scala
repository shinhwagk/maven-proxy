package org.gk.server

import java.net.ServerSocket
import org.gk.server.workers._
import org.gk.server.config.cfg


/**
 * Created by gk on 15/7/21.
 */
object ProxyServer extends App {

  val ss = new ServerSocket(cfg.getMavenProxyServicePost);

  import org.gk.server.db._

  InitDatabase.initMavenProxy

  println("系统已经启动...")

  while (true) {
    val socket = ss.accept()
    ActorRefWokerGroups.doorman ! socket
  }
}
