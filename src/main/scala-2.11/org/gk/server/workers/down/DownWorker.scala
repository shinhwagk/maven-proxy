package org.gk.server.workers.down

import java.io.{OutputStreamWriter, BufferedWriter, RandomAccessFile}
import java.net.{InetSocketAddress, Socket, HttpURLConnection, URL}

import akka.actor.{Actor, ActorLogging, _}
import org.gk.server.workers.RequestHeaders
import org.gk.server.workers.down.DownMaster.WorkerDownSectionSuccess
import org.gk.server.workers.down.DownWorker.WorkerDownSelfSection

/**
 * Created by goku on 2015/7/28.
 */

object DownWorker {

  case object Downming

  case class WorkerDownSelfSection(workerNumber: Int, fileUrl: String, startIndex: Int, endIndex: Int)

  def storeWorkFile(fileTempOS: String, startIndex: Int, buffer: Array[Byte]) = synchronized {
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}

class DownWorker(downMasterActorRef: ActorRef) extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case parameter@WorkerDownSelfSection(workerNumber, fileURL, startIndex, endIndex) => {

      //      log.debug("线程: {} 下载{};收到,开始下载...",workerNumber,fileURL)
      val downResult = down(parameter)
      downMasterActorRef ! WorkerDownSectionSuccess(downResult._1, downResult._2)
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor:" + self.path + ", preRestart parent, reason:" + reason + ", message:" + message)

    self ! message.get.asInstanceOf[WorkerDownSelfSection]
  }

  override def postRestart(reason: Throwable) {
    log.debug("actor:{}, postRestart parent, reason:{}", self.path, reason)
  }

  def down(parameter: WorkerDownSelfSection): (Int, Array[Byte]) = {
    //    log.info("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val fileUrl = parameter.fileUrl
    val startIndex = parameter.startIndex
    val endIndex = parameter.endIndex
    val workerNumber = parameter.workerNumber

    val url = new URL(fileUrl);
    val host = url.getHost();
    val port = url.getDefaultPort()

    val socket = new Socket();
    val address = new InetSocketAddress(host, 80);
    socket.setSoTimeout(20000)
    socket.connect(address, 10000);
    val bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
    bufferedWriter.write("GET " + url.getFile() + " HTTP/1.1\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("Accept: */*\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("Connection: Keep-Alive\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("Range: bytes=" + startIndex + "-" + endIndex + "\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("Host: " + host + "\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("\r\n"); // 请求头信息发送结束标志
    bufferedWriter.flush()
    val aa = new RequestHeaders(socket)
    val is = aa.bis


    //    val is = downConn.getInputStream();
    val workFileLength = aa.Head_ContentLength.get.toInt;

//    println(startIndex + "         " + endIndex + "      " + (endIndex - startIndex) + "   " + workFileLength)
    var currentLength = 0
    var start = 0
    var len = 0
    val buffer = new Array[Byte](workFileLength)
    try {

      while (len != -1 && workFileLength != currentLength) {
        len = is.read(buffer, start, workFileLength - currentLength)
        start += len
        currentLength += len
        //              log.info("{}下载完成进度:{}/{}",fileUrl,currentLength, workFileLength)
        //      log.debug("线程: {};下载文件{}，进度 {}/{} ...",thread,url,currentLength,workFileLength)
      }
    } finally is.close()

    //    log.info("ActorRef:{}; 下载完毕", self.path.name)
    //    downConn.disconnect()
    (workerNumber, buffer)
  }
}