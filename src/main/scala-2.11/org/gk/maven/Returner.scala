package org.gk.maven

import java.io.{BufferedInputStream, BufferedOutputStream, File, FileInputStream}
import java.net.Socket
import java.util.Date

import akka.actor.Actor
import org.gk.server.workers.ActorRefWorkerGroups


/**
 * Created by gk on 15/7/26.
 */

object Returner {

  case class RuntrunFile(socket: Socket, fileOS: String)

}

class Returner extends Actor with akka.actor.ActorLogging {

  override def receive: Receive = {
    case (socket:Socket, fileOS:String) =>
      val fileHeadler = new File(fileOS)
      if (fileHeadler.exists())
        sendFile(fileOS, socket)
      else
        ActorRefWorkerGroups.terminator !(404, socket)
  }

  def sendFile(fileOS: String, socket: Socket) = {
    val bis = new BufferedInputStream(new FileInputStream(new File(fileOS)));
    val bos = new BufferedOutputStream(socket.getOutputStream());

    val fileLength = bis.available();
    val buffer = new Array[Byte](fileLength);

    bis.read(buffer);

    bos.write(getHeaderBytes(fileLength));
    bos.write(buffer);
    bos.flush();

    bis.close()
    bos.close()
    ActorRefWorkerGroups.terminator ! socket
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
