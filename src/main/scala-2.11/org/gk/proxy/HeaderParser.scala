package org.gk.proxy

import java.io.BufferedInputStream
import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.download.DownManager
import org.gk.repository.RepoManager.{DownFileFailure, DownFileSuccess}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by gk on 15/8/22.
 */
class HeaderParser extends Actor {
  val downManager = context.actorOf(Props(new DownManager(self)), name = "HeaderParser_DownManager")

  override def receive: Receive = {
    case socket: Socket =>
      val requestLine = getRequestLine(socket)
      requestLine(0) match {
        case "GET" =>
          println("get")
          downManager !(socket, requestLine(1))
        case "CONNECT" =>
      }
    case DownFileSuccess(fileUrl, fileArrayByte) =>


    case DownFileFailure(fileUrl, code) =>


  }


  def getRequestLine(socket: Socket): Array[String] = {
    val bis = new BufferedInputStream(socket.getInputStream)

    val tempByteBuffer = new ArrayBuffer[Int]
    val dividingLine = ArrayBuffer(13, 10) //\n\r
    while (tempByteBuffer.takeRight(2) != dividingLine) {
      tempByteBuffer += bis.read()
    }
    tempByteBuffer.trimEnd(4)
    val requestLine = new String(tempByteBuffer.map(_.toByte).toArray)
    requestLine.split(" ")
  }
}
