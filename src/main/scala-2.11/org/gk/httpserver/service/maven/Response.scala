package org.gk.httpserver.service.maven

import java.net.Socket
import java.util.Date

import akka.actor.Actor
import akka.actor.Actor.Receive
import java.io.{BufferedOutputStream, BufferedInputStream, FileInputStream, File}

import org.gk.config.cfg
import org.gk.log.GkConsoleLogger
import org.gk.httpserver.CaseResponse

/**
 * Created by goku on 2015/7/23.
 */
class Response extends Actor{
  override def receive: Receive = {
    case CaseResponse(path,socket) =>{
      println(path)
      GkConsoleLogger.info("Response收到请求,开始处理...")
      abcxx(path,socket)
      GkConsoleLogger.info("发送完毕;")
      socket.close()
      sender() ! "over"
    }
  }


  def abcxx (path:String,socket:Socket): Unit ={
    val filepath = cfg.getLocalRepositoryDir + path
    val file = new File(filepath)
    var fis = new FileInputStream(file);
    var bis = new BufferedInputStream(fis);
    var bislength = bis.available();
    var os = socket.getOutputStream();
    var bos = new BufferedOutputStream(os);
    var sb = new StringBuilder();
    sb.append("HTTP/1.1 200 OK\n");
//    sb.append("Content-Type: application/java-archive\n");
    sb.append("Content-Type: application/octet-stream\n");
    sb.append("Date: " + new Date() + "\n");
    sb.append("Content-Length: " + (bislength) + "\n");
    sb.append("Accept-Ranges: bytes\n");
    sb.append("Connection: Keep-Alive\n")
    sb.append("Keep-Alive: true\n");

    sb.append("\n");
    bos.write(sb.toString().getBytes);
    var buffer = new Array[Byte](bislength);
    bis.read(buffer, 0, bislength);
    bos.write(buffer); // 返回文件数据
    GkConsoleLogger.info("测试1")
    bos.flush();
    GkConsoleLogger.info("测试2")
  }
}
