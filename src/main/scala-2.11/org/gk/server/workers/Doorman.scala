package org.gk.server.workers

import java.io.BufferedInputStream
import java.net.Socket

import akka.actor.{Actor, ActorLogging}
import org.gk.server.workers.RepoManager.RequertFile

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/24.
 */

class Doorman extends Actor with ActorLogging {

  override def receive: Receive = {
    case socket: Socket => ActorRefWorkerGroups.repoManager ! RequertFile(RequestHeader(socket))
  }
}

case class RequestHeader(s: Socket) {

  val socket = s

  private val bis = new BufferedInputStream(socket.getInputStream)

  private lazy val headerBytes: Array[Byte] = {
    val tempByteBuffer = new ArrayBuffer[Int]
    val dividingLine = ArrayBuffer(13, 10, 13, 10) //\n\r
    while (tempByteBuffer.takeRight(4) != dividingLine) {
      tempByteBuffer += bis.read()
    }
    tempByteBuffer.trimEnd(4)
    tempByteBuffer.map(_.toByte).toArray
  }

  lazy val headerString: String = {
    val a = new String(headerBytes)
    println(a)
    a
  }

  lazy val headerList: List[String] = headerString.split("\r\n").toList

  lazy val filePath = headerString.split("\r\n")(0).split(" ")(1)
}
