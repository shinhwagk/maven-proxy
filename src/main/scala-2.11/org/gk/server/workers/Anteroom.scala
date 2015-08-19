package org.gk.server.workers

import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.server.config.cfg
import org.gk.server.tool.RequestHeader
import org.gk.server.workers.Anteroom._
import org.gk.server.workers.Returner.RuntrunFile
import org.gk.server.workers.down.DownManager.RequertDownFile

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/8/17.
 */

object Anteroom {
  case class JoinAnteroom(requestHeader: RequestHeader)

  case class DBFileCreate(fileOS: String, value: Socket)

  case class DBFileDelete(fileOS: String)

  case class LeaveAnteroom(filePath: String)

}

class Anteroom extends Actor {

  private var requertFileMap: Map[String, ArrayBuffer[Socket]] = Map.empty

  override def receive: Receive = {
    case JoinAnteroom(requestHeader) =>
      val fileOS = cfg.getLocalMainDir + requestHeader.filePath
      val socket = requestHeader.socket
      if (requertFileMap.contains(fileOS)) {
        val socketArrayBuffer = requertFileMap(fileOS)
        socketArrayBuffer += socket
      } else {
        val socketArrayBuffer = new ArrayBuffer[Socket]()
        socketArrayBuffer += socket
        requertFileMap += (fileOS -> socketArrayBuffer)
        ActorRefWorkerGroups.downManager ! RequertDownFile(requestHeader)
      }

    case LeaveAnteroom(fileOS) =>
      val socketArrayBuffer = requertFileMap(fileOS)
      requertFileMap -= (fileOS)
      socketArrayBuffer.foreach(p => {
        val socket = p
        context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(socket, fileOS)
      })
  }
}


