package org.gk.repository

import java.io.BufferedOutputStream
import java.net.Socket
import java.util.Date

import akka.actor.Actor


/**
 * Created by gk on 15/7/26.
 */

object Returner {

  case class RuntrunFile(socket: Socket, fileOS: String)

}

class Returner extends Actor with akka.actor.ActorLogging {

  override def receive: Receive = {
    case (socket: Socket, fileArrayByte: Array[Byte]) =>
      sendFile(socket, fileArrayByte)
  }

  def sendFile(socket: Socket, fileArrayByte: Array[Byte]) = {
    val bos = new BufferedOutputStream(socket.getOutputStream());

    bos.write(getHeaderBytes(fileArrayByte.length));
    bos.write(fileArrayByte);
    bos.flush();
    bos.close()
    //    ActorRefWorkerGroups.terminator ! socket
  }

  def getHeaderBytes(fileLength: Int): Array[Byte] = {
    val sb = new StringBuilder();
    sb.append("HTTP/1.1 200 OK\r\n");
    sb.append("Content-Type: application/octet-stream\r\n");
    sb.append("Date: " + new Date() + "\r\n");
    sb.append("Content-Length: " + (fileLength) + "\r\n");
    sb.append("Accept-Ranges: bytes\r\n");
    sb.append("Connection: Keep-Alive\r\n")
    sb.append("Keep-Alive: true\r\n");
    sb.append("\r\n");
    sb.toString().getBytes
  }
}
