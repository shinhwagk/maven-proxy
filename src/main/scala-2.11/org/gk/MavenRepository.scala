package org.gk

import java.net.{Socket, ServerSocket}

import akka.actor.{Props, Actor}
import org.gk.repository.HeaderParser
import org.gk.server.config.cfg

/**
 * Created by goku on 2015/8/24.
 */
class MavenRepository extends Actor {

  val headerParser = context.actorOf(Props[HeaderParser], name = "HeaderParser")

  val ss = new ServerSocket(cfg.getMavenProxyServicePost);

  while (true) {
    headerParser !(ss.accept(), "repo")
    println("收到请求repo....")
  }

  override def receive: Receive = {
    case socket: Socket =>
  }
}
