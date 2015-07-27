package org.gk.httpserver.service.workers

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import akka.actor.{Props, Actor}

/**
 * Created by goku on 2015/7/27.
 */
class HeadParser extends Actor {
  val terminator = context.actorOf(Props[Terminator])
  val repoManager = context.actorOf(Props[RepoManager])

  override def receive: Receive = {
    case socket:Socket =>{
      headParse(socket)
    }
  }

  def headParse(socket: Socket): Unit ={
    val headBuffers = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val headFirstLine = headBuffers.readLine()
    headFirstLine match {
      case null => terminator ! (204,socket)
      case _ if headFirstLine.split(" ").length !=3 => terminator ! (204,socket)
      case _ if headFirstLine.split(" ")(1) == "/" => terminator ! (204,socket)
      case _ => repoManager ! (headFirstLine.split(" ")(1),socket)
    }
  }
}