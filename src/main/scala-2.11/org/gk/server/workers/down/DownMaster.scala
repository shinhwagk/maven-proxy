package org.gk.server.workers.down

import java.net.{HttpURLConnection, URL}

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.server.workers.down.DownManager.DownFileSuccess
import org.gk.server.workers.down.DownWorker.WorkerDownSelfSection
import org.gk.server.workers.{ActorRefWorkerGroups, DownFileInfo}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/28.
 */
object DownMaster {

  case class DownFile(fileUrl: String, file: String)

  case class WorkerDownSectionSuccess(downFileInfo: DownFileInfo, workerNumber: Int)

}

case class Download(downFileInfo: DownFileInfo)

case class RequertGetFile(downFileInfo: DownFileInfo)

class DownMaster extends Actor with ActorLogging {

  import DownMaster._

  var workerSuccessCount: Int = _

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 50, withinTimeRange = 60 seconds) {
    case _: Exception => Restart
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor:" + self.path + ", preRestart parent, reason:" + reason + ", message:" + message)
    self ! message.get.asInstanceOf[Download]
  }

  override def postRestart(reason: Throwable) {
    log.debug("actor:{}, postRestart parent, reason:{}", self.path, reason)
  }

  override def receive: Receive = {
    case Download(downFileInfo) =>
      println("进入下载")
      val url = downFileInfo.fileUrl
      val downUrl = new URL(url);
      val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
      val responseCode = downConn.getResponseCode
      responseCode match {
        case 404 =>
          ActorRefWorkerGroups.terminator !(404, downFileInfo.socket)
        case 200 =>
          downFileInfo.fileLength = downConn.getContentLength
          allocationWorker(downFileInfo)
      }
      downConn.disconnect()

    case WorkerDownSectionSuccess(downFileInfo, workerNumber) =>
      val filePath = downFileInfo.filePath
      val workerSize = downFileInfo.workerDownInfo.size

      workerSuccessCount += 1

      println(workerSuccessCount + "/" + workerSize)
      if (workerSuccessCount == workerSize) {
        storeWorkFile(downFileInfo)
        ActorRefWorkerGroups.downManager ! DownFileSuccess(filePath)
      }
    case Terminated(actorRef) =>
      println(actorRef.path.name + "被中置")
  }

  def allocationWorker(downFileInfo: DownFileInfo): Unit = {

    val file = downFileInfo.filePath
    val fileUrl = downFileInfo.fileUrl
    val fileLength = downFileInfo.fileLength
    val filePath = downFileInfo.filePath
    val downWokerAmount = downFileInfo.workerNumber


    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, downWokerAmount)

    for (i <- 1 to downWokerAmount) {
      context.watch(context.actorOf(Props(new DownWorker(self)))) ! WorkerDownSelfSection(downFileInfo, i)
      log.debug("线程: {} 下载请求已经发送...", i)
    }
  }

  def storeWorkFile(downFileInfo: DownFileInfo) = {
    import java.io._
    val fileHeadle = new File(downFileInfo.fileOS)
    if (!fileHeadle.getParentFile.exists()) {
      fileHeadle.getParentFile.mkdirs()
    }
    val raf = new RandomAccessFile(downFileInfo.fileOS, "rwd")
    raf.setLength(downFileInfo.fileLength)
    val fileBuffer = new ArrayBuffer[Byte]()
    downFileInfo.workerDownInfo.toList.sortBy(_._1).map(l => {
      val buffer = l._2._3
      fileBuffer ++= buffer
    })
    val buffer = fileBuffer.toArray
    raf.write(buffer)
    raf.close()
  }
}