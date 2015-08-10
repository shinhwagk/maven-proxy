package org.gk.server.workers

import java.io.{BufferedOutputStream, BufferedInputStream, FileInputStream, File}
import java.lang.Terminator
import java.util.Date

import akka.actor.{Props, Actor}
import java.net.Socket


/**
 * Created by gk on 15/7/26.
 */

case class RuntrunFile(downFileInfo:DownFileInfo)
class Returner extends Actor with akka.actor.ActorLogging{

  val terminator = context.actorOf(Props[Terminator])

  override def receive: Receive = {
    case RuntrunFile(downFileInfo) => {
      val fileOS = downFileInfo.fileOS
      val socket = downFileInfo.socket
      log.info("准备发送文件{}。。。",fileOS)
      val bis = new BufferedInputStream(new FileInputStream(new File(fileOS)));
      val downFileLength = bis.available();
      val bos = new BufferedOutputStream(socket.getOutputStream());

      bos.write(getHeaderBytes(downFileLength));

      val buffer = new Array[Byte](downFileLength);
      bis.read(buffer, 0, downFileLength);
      bos.write(buffer);
      bos.flush();

      bis.close()
      socket.close()
      log.info("文件:{},已经返回给请求者",fileOS)
      sender() ! "ffs"
//      context.unwatch(sender())
//      context.stop(sender())
//      terminator ! socket
    }
  }

  def getHeaderBytes(fileLength:Int): Array[Byte] ={
    val sb = new StringBuilder();
    sb.append("HTTP/1.1 200 OK\n");
    //    sb.append("Content-Type: application/java-archive\n");
    sb.append("Content-Type: application/octet-stream\n");
    sb.append("Date: " + new Date() + "\n");
    sb.append("Content-Length: " + (fileLength) + "\n");
    sb.append("Accept-Ranges: bytes\n");
    sb.append("Connection: Keep-Alive\n")
    sb.append("Keep-Alive: true\n");
    sb.append("\n");
    sb.toString().getBytes
  }
}
