package org.gk.server.workers.down

import java.io.RandomAccessFile
import java.net.{HttpURLConnection, URL}

import akka.actor.{Actor, ActorLogging, _}
import org.gk.server.db.DML
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables._
import org.gk.server.workers.DownFileInfo
import org.gk.server.workers.down.DownMaster.WorkerDownSectionSuccess
import org.gk.server.workers.down.DownWorker.WorkerDownSelfSection
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by goku on 2015/7/28.
 */

object DownWorker {

  case object Downming

  case class WorkerDownSelfSection(fileUrl:String,buffer:Array[Byte],startIndex:Int,endIndex:Int)

  def storeWorkFile(fileTempOS: String, startIndex: Int, buffer: Array[Byte]) = synchronized {
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}

class DownWorker(downMasterActorRef:ActorRef) extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case WorkerDownSelfSection(fileURL,buffer,startIndex,endIndex) => {
      //      log.debug("线程: {} 下载{};收到,开始下载{}...",thread,url,fileTmpOS)
      sender()! WorkerDownSectionSuccess(down(fileURL,buffer,startIndex,endIndex))
      //      log.debug("线程: {} 下载{};完毕{}...",thread,url,fileTmpOS)
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor:" + self.path + ", preRestart parent, reason:" + reason + ", message:" + message)
    self ! message.get.asInstanceOf[WorkerDownSelfSection]
  }

  override def postRestart(reason: Throwable) {
    log.debug("actor:{}, postRestart parent, reason:{}", self.path, reason)
  }

  def down(fileURL:String,buffer:Array[Byte],startIndex:Int,endIndex:Int):Int= {

    //    log.info("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(fileURL);
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(3000)
    downConn.setReadTimeout(3000)
    downConn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
    downConn.setRequestProperty("Accept-Encoding", "gzip")
    downConn.setRequestProperty("Cache-control", "no-cache")
    downConn.setRequestProperty("Cache-store", "no-cache")
    downConn.setRequestProperty("Pragma", "no-cache")
    downConn.setRequestProperty("Expires", "0")
    downConn.setRequestProperty("Connection", "Keep-Alive")


    val is = downConn.getInputStream();

    try {
      val workFileLength = downConn.getContentLength;

      var currentLength = 0
      var start = 0
      var len = 0

      while (len != -1 && workFileLength != currentLength) {
        len = is.read(buffer, start, workFileLength - currentLength)
        start += len
        currentLength += len
        //      log.info("{}下载完成进度:{}/{}",url,currentLength, workFileLength)
        //      log.debug("线程: {};下载文件{}，进度 {}/{} ...",thread,url,currentLength,workFileLength)
      }
    } finally is.close()

    log.debug("ActorRef:{}; 下载完毕", self.path.name)
    downConn.disconnect()
    1
  }
}