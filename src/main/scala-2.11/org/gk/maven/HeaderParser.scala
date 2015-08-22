package org.gk.maven

import java.io.BufferedInputStream
import java.net.Socket

import akka.actor.Actor
import org.gk.server.workers.ActorRefWorkerGroups

import scala.collection.mutable.ArrayBuffer

/**
 * Created by gk on 15/8/22.
 */
class HeaderParser extends Actor {
  override def receive: Receive = {
    case socket: Socket =>
      ActorRefWorkerGroups.repoManager ! (socket,getFilePath(socket))
  }


  def getFilePath(socket: Socket): String = {
    val bis = new BufferedInputStream(socket.getInputStream)

    val tempByteBuffer = new ArrayBuffer[Int]
    val dividingLine = ArrayBuffer(13, 10) //\n\r
    while (tempByteBuffer.takeRight(2) != dividingLine) {
      tempByteBuffer += bis.read()
    }
    tempByteBuffer.trimEnd(4)
    val requestLine = new String(tempByteBuffer.map(_.toByte).toArray)
    requestLine.split(" ")(1)
  }
}
