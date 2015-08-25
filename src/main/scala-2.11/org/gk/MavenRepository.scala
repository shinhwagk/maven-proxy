package org.gk

import java.net.ServerSocket

import akka.actor.{Actor, Props}
import org.gk.server.config.cfg

/**
 * Created by goku on 2015/8/24.
 */
class MavenRepository extends Actor {

  var ss: ServerSocket = _

  override def receive: Receive = {
    case "start" =>
      ss = new ServerSocket(cfg.getMavenProxyServicePost);

      while (true) {
        val socket = ss.accept()
        MavenProxyMain.headerParser !(socket, "repo")
        println("收到请求repo....")
      }

    case "stop" =>
      ss.close()
  }
}
