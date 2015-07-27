package org.gk.workers

import java.io.BufferedOutputStream

import akka.actor.Actor
import akka.actor.Actor.Receive
import java.net.Socket;

/**
 * Created by gk on 15/7/26.
 */
class Terminator extends Actor with akka.actor.ActorLogging{
  override def receive: Receive = {
    case socket:Socket => {
      socket.close()
      log.debug("连接关闭...")
    }
  }
}
