package org.gk

import java.net.ServerSocket

import akka.actor.{Actor, Props}
import org.gk.server.config.cfg

/**
 * Created by goku on 2015/8/24.
 */
class MavenHttpProxy extends Actor {

  val headerParser = context.actorOf(Props[HeaderParser], name = "HeaderParser")

  var ss: ServerSocket = _
  override def receive: Receive = {
    case "start" =>
      ss = new ServerSocket(cfg.getMavenProxyServicePost + 10);

      while (true) {
        val socket = ss.accept()
        headerParser !(socket, "proxy")
        println("收到请求Http....")
      }

    case "stop" =>
      ss.close()
  }
}
