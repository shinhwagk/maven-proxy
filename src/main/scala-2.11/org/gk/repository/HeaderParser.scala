package org.gk.repository

import java.io.BufferedInputStream
import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.proxy.ProxyMain

import scala.collection.mutable.ArrayBuffer

/**
 * Created by gk on 15/8/22.
 */
class HeaderParser extends Actor {
  val repoManager = context.actorOf(Props[RepoManager], name = "RepoManager")
  val repositoryMain = context.actorOf(Props[RepositoryMain], name = "RepositoryMain")
  val proxyMain = context.actorOf(Props[ProxyMain], name = "ProxyMain")

  override def receive: Receive = {
    case (socket: Socket, "repo") =>
      val requestLine = getFilePath(socket)
      val filePath = requestLine(1)
      //      repositoryMain ! SreachFile(socket, filePath)
      println("请求:" + filePath)

    case (socket: Socket, "proxy") =>
      val requestLine = getFilePath(socket)
      proxyMain !(socket, requestLine)
  }


  def getFilePath(socket: Socket): String = {
    val bis = new BufferedInputStream(socket.getInputStream)

    val tempByteBuffer = new ArrayBuffer[Int]
    val dividingLine = ArrayBuffer(13, 10) //\n\r
    while (tempByteBuffer.takeRight(2) != dividingLine) {
      tempByteBuffer += bis.read()
    }
    tempByteBuffer.trimEnd(4)
    new String(tempByteBuffer.map(_.toByte).toArray)

  }
}
