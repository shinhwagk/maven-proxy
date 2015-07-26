package org.gk.httpserver.service.workers

import java.io.{InputStreamReader, BufferedReader}

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import java.net.Socket

/**
 * Created by gk on 15/7/26.
 */
class HeadFormater extends Actor {
  val headParser = context.actorOf(Props[HeadParser], name = "headparser")

  override def receive: Receive = {
    case socket:Socket => {

    }
  }

  def headFormat(socket:Socket)={
    val headBuffers = new BufferedReader(new InputStreamReader(socket.getInputStream))
    var headLine = headBuffers.readLine()
    while(headLine != null && !headLine.isEmpty){

    }


  }
}
