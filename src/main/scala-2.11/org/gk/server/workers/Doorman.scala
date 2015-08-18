package org.gk.server.workers

import java.io.BufferedInputStream
import java.net.Socket

import akka.actor.{Actor, ActorLogging}
import org.gk.server.workers.RepoManager.RequertFile

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/24.
 */
object Doorman {

  case class StoreRequert(filePath: String, socket: Socket)

}


//检查数据库
class Doorman extends Actor with ActorLogging{

  override def receive: Receive = {
    case socket: Socket =>
      println("xx")
      val requestHeader = RequestHeader(socket)
      ActorRefWorkerGroups.repoManager ! RequertFile(requestHeader)
  }
}


case class RequestHeader(s: Socket) {

  val socket = s

  private val bis = new BufferedInputStream(socket.getInputStream)

  private lazy val headerBytes: Array[Byte] = {
    val tempByteBuffer = new ArrayBuffer[Int]
    val dividingLine = ArrayBuffer(13, 10, 13, 10)
    while (tempByteBuffer.takeRight(4) != dividingLine) {
      tempByteBuffer += bis.read()
    }
    tempByteBuffer.trimEnd(4)
    tempByteBuffer.map(_.toByte).toArray
  }

  lazy val headerString: String = {
    println("xxx")
    val a = new String(headerBytes)
    println(a)
    a
  }

  lazy val headerList: List[String] = headerString.split("\r\n").toList

  lazy val filePath = headerList.find(p => p.startsWith("GET") || p.startsWith("HEAD")).get.split(" ")(1)
}
