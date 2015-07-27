package org.gk.workers

import java.net.Socket
import java.util.Date

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import java.io.{BufferedOutputStream, BufferedInputStream, FileInputStream, File}

import org.gk.CaseResponse
import org.gk.config.cfg
import org.gk.downfile.{CaseDownFileOk, DownFile}
import org.gk.log.GkConsoleLogger

/**
 * Created by goku on 2015/7/23.
 */
object Response{
  def props(socket:Socket):Props = Props(new Response(socket))
}

class Response(socket:Socket) extends Actor{
  val downfile = context.actorOf(Props[DownFile],name ="DownFile2")

  override def receive: Receive = {
    case CaseResponse(filepath,socket) =>{
      GkConsoleLogger.info("Response收到请求,开始处理...")
      GkConsoleLogger.info("Response收到请求: 检查 "+ filepath + "是否存在本地中...")
      if(!DecideLocalFileExists(filepath)){
        GkConsoleLogger.info("Response收到请求: 文件 "+ filepath + "不在本地仓库中...")
        GkConsoleLogger.info("Response收到请求: 发送下载请求...")
//        downfile ! CaseDownFile(filepath,socket)
      }else{
        GkConsoleLogger.info("Response收到请求: 发现 "+ filepath + "存在本地中...")
        sendFile(filepath, socket)
        GkConsoleLogger.info("发送完毕;")
        socket.close()
        sender() ! "over"
      }

    }
    case CaseDownFileOk(filepath,socket) =>{
      GkConsoleLogger.info("下载处理者: 收到下载下载完成通知...")
      GkConsoleLogger.info("Response收到请求: " + filepath + "存在本地仓库中...")
      sendFile(filepath, socket)
      GkConsoleLogger.info("发送完毕;")
      socket.close()
      sender() ! "over"
    }
  }

  def DecideLocalFileExists(filePath:String): Boolean ={
    new File(org.gk.config.cfg.getLocalRepoDir + filePath).exists()
  }
  def sendFile (filepath:String,socket:Socket): Unit ={
    val fileUrl = cfg.getLocalRepoDir + filepath
    val file = new File(fileUrl)
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
    bos.flush();
    GkConsoleLogger.info("文件发送完毕..................")
  }
}
