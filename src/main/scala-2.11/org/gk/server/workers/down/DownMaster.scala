package org.gk.server.workers.down

import java.net.{HttpURLConnection, URL}

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.server.config.cfg
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import org.gk.server.workers.ActorRefWorkerGroups
import org.gk.server.workers.down.DownManager.DownFileSuccess
import org.gk.server.workers.down.DownWorker.WorkerDownSelfSection
import slick.driver.H2Driver.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/28.
 */
object DownMaster {

  case class DownFile(fileUrl: String, file: String)

  case class WorkerDownSectionSuccess(workerNumber: Int, Buffer: Array[Byte])

  case class Download(filePath: String)

}


class DownMaster extends Actor with ActorLogging {

  import DownMaster._

  var workerSuccessCount: Int = _
  var fileUrlLength: Int = _
  var fileUrl: String = _
  lazy val workerAmount: Int = getDownWorkerNumber
  var filePath: String = _
  var downSuccessCount: Int = _
  lazy val fileOS = cfg.getLocalMainDir + filePath
  var downSuccessSectionBufferMap: Map[Int, Array[Byte]] = Map.empty

  override def receive: Receive = {
    case Download(filePath) =>
      println("进入下载")
      fileUrl = getFileUrl(filePath)
      this.filePath = filePath
      val downUrl = new URL(fileUrl);
      val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
      val responseCode = downConn.getResponseCode
      log.info("测试下载地址:{}.ResponseCode", downUrl)
      println(downUrl)
      responseCode match {
        case 404 =>
          ActorRefWorkerGroups.terminator !(404, filePath)
        case 200 =>

          fileUrlLength = downConn.getContentLength
          println("xxxxxxxxxx"+ fileUrlLength)
          startWorkerDown
      }
      downConn.disconnect()

    case WorkerDownSectionSuccess(workerNumber, fileSectionBuffer) =>
      workerSuccessCount += 1
      downSuccessSectionBufferMap += (workerNumber -> fileSectionBuffer)
      println("下载完成----:"+workerSuccessCount + "/" + workerAmount)
      if (workerSuccessCount == workerAmount) {
        log.info("文件:{}.下载完毕", filePath)
        storeWorkFile
        ActorRefWorkerGroups.downManager ! DownFileSuccess(filePath)
      }
    case Terminated(actorRef) =>
      println(actorRef.path.name + "被中置")
  }


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

  def startWorkerDown: Unit = {
    //    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, downWokerAmount)
//    for (i <- 1 to workerAmount) {
//      val endLength = fileUrlLength % workerAmount
//      val step = (fileUrlLength - endLength) / workerAmount
////      val startIndex: Int = (i - 1) * step
//      val startIndex: Int = i
////      val endIndex = if (i == workerAmount) i * step + endLength - 1 else i * step - 1
////      println(startIndex +"~"+ endIndex +"/" + workerAmount + "/" + fileUrlLength)
//      val endIndex = fileUrlLength
//      context.watch(context.actorOf(Props(new DownWorker(self)))) ! WorkerDownSelfSection(i, fileUrl, startIndex, endIndex)
      context.watch(context.actorOf(Props(new DownWorker(self)))) ! WorkerDownSelfSection(1, fileUrl, 0, 376)
//      log.debug("线程: {} 下载请求已经发送...", i)
//    }
  }

  def storeWorkFile = {
    import java.io._
    val fileHeadle = new File(fileOS)
    if (!fileHeadle.getParentFile.exists()) {
      fileHeadle.getParentFile.mkdirs()
    }
    val raf = new RandomAccessFile(fileOS, "rwd")
//    println("aaa" + fileUrlLength)
//    raf.setLength(fileUrlLength)
    val fileBuffer = new ArrayBuffer[Byte]()
    downSuccessSectionBufferMap.toList.sortBy(_._1).map(l => {
      val buffer = l._2
      fileBuffer ++= buffer
    })
    val buffer = fileBuffer.toArray
    raf.write(buffer)
    raf.close()
  }

  private def getFileUrl(filePath: String): String = {
    val repoName = filePath.split("/")(1)
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    filePath.replace("/" + repoName + "/", repoUrl + "/")
  }

  private def getDownWorkerNumber: Int = {
    val processForBytes = cfg.getPerProcessForBytes
    println(fileUrlLength + "xxx" + processForBytes)
    if (fileUrlLength >= processForBytes) fileUrlLength / processForBytes else 1
  }
}