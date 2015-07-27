package org.gk.workers

import java.io.{BufferedOutputStream, BufferedInputStream, FileInputStream, File}
import java.util.Date

import akka.actor.{Props, Actor}
import java.net.Socket

/**
 * Created by gk on 15/7/26.
 */
class Sender extends Actor with akka.actor.ActorLogging{
  val terminator = context.actorOf(Props[Terminator])
  override def receive: Receive = {
    case (osFile:String,socket:Socket) => {
      log.debug("准备发送文件{}。。。",osFile)
      val file = new File(osFile)
      val bis = new BufferedInputStream(new FileInputStream(file));
      val bislength = bis.available();
      val bos = new BufferedOutputStream(socket.getOutputStream());

      val sb = new StringBuilder();
      sb.append("HTTP/1.1 200 OK\n");
      //    sb.append("Content-Type: application/java-archive\n");
      sb.append("Content-Type: application/octet-stream\n");
      sb.append("Date: " + new Date() + "\n");
      sb.append("Content-Length: " + (bislength) + "\n");
      sb.append("Accept-Ranges: bytes\n");
      sb.append("Connection: Keep-Alive\n")
      sb.append("Keep-Alive: true\n");
      sb.append("\n");
      log.debug("发送头文件编辑完毕。。。")
      bos.write(sb.toString().getBytes);
      log.debug("发送头文件。。。")
      val buffer = new Array[Byte](bislength);
      bis.read(buffer, 0, bislength);
      bos.write(buffer);
      log.debug("发送文件。。。")
      bos.flush();
      log.debug("发送文件OK。。。")
//      bos.close()
//      bis.close()
      terminator ! socket
    }
  }
}
