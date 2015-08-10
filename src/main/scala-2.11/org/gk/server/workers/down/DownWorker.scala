package org.gk.server.workers.down

import java.net.URL
import akka.actor.{ActorLogging, Actor}
import org.gk.server.db.DML
import org.gk.server.workers.DownFileInfo
import org.gk.workers.DownFileInfo
import org.gk.workers.down.DownMaster.WorkerDownSectionSuccess
import org.gk.workers.down.DownWorker.WorkerDownSelfSection
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import org.gk.db.MetaData._
import org.gk.db.Tables._
import akka.actor._
import org.gk.workers.down.DownMaster._
import slick.driver.H2Driver.api._
import scala.concurrent.duration.Duration
import scala.concurrent.Await

/**
 * Created by goku on 2015/7/28.
 */

object DownWorker {

  case object Downming

  case class WorkerDownSelfSection(downFileInfo: DownFileInfo, workerNumber: Int)

  def storeWorkFile(fileTempOS: String, startIndex: Int, buffer: Array[Byte]) = synchronized {
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}

class DownWorker(downMasterActorRef:ActorRef) extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case WorkerDownSelfSection(downFileInfo, downWokerNumber) => {
      //      log.debug("线程: {} 下载{};收到,开始下载{}...",thread,url,fileTmpOS)
      down(downFileInfo, downWokerNumber)
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

  def down(downFileInfo: DownFileInfo, workerNumber: Int) = {
    val startIndex = downFileInfo.workerDownInfo(workerNumber)._1
    val endIndex = downFileInfo.workerDownInfo(workerNumber)._2
    val url = downFileInfo.fileUrl
    val fileTmpOS = downFileInfo.fileTempOS

    //    log.info("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url);
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

      val buffer = new Array[Byte](workFileLength)
      while (len != -1 && workFileLength != currentLength) {
        len = is.read(buffer, start, workFileLength - currentLength)
        start += len
        currentLength += len
        //      log.info("{}下载完成进度:{}/{}",url,currentLength, workFileLength)
        //      log.debug("线程: {};下载文件{}，进度 {}/{} ...",thread,url,currentLength,workFileLength)
      }
      storeWorkFile(fileTmpOS, startIndex, buffer)
    } finally is.close()

    log.debug("ActorRef:{}; 下载完毕", self.path.name)
    downConn.disconnect()
    Await.result(db.run(downFileWorkList.filter(_.fileUrl === url).filter(_.startIndex === startIndex).map(p => (p.success)).update(1)), Duration.Inf)
    val ccc = Await.result(db.run(downFileWorkList.filter(_.fileUrl === url).map(p => (p.success)).result), Duration.Inf).toList.sum

    import DML._
    val fileDownNumber = selectDownNumber(url)
    println("wancheng " + ccc + "/" + fileDownNumber)
    if (ccc == fileDownNumber) {
      println("下载完成.....")
      downMasterActorRef ! WorkerDownSectionSuccess(downFileInfo)
    }
  }
}