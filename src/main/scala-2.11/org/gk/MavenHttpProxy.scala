package org.gk

import java.net.{Socket, ServerSocket}

import akka.actor.{Props, Actor}
import org.gk.repository.HeaderParser
import org.gk.server.config.cfg

/**
 * Created by goku on 2015/8/24.
 */
class MavenHttpProxy extends Actor {

  val headerParser = context.actorOf(Props[HeaderParser], name = "HeaderParser")

  val ss = new ServerSocket(cfg.getMavenProxyServicePost + 10);

  while (true) {
    headerParser !(ss.accept(), "proxy")
    println("收到请求Http....")
  }

  override def receive: Receive = {
    case socket: Socket =>
      headerParser !(socket, "proxy")
  }
}
