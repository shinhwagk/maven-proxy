package org.gk.workers.down

import java.io.{File, RandomAccessFile}
import java.net.HttpURLConnection
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.DownFileInfo
import org.gk.workers.down.DownManager.DownFileSuccess
import org.gk.workers.down.DownWorker.{WorkerDownSelfSection, Downming}
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import org.gk.db.DML._

/**
 * Created by goku on 2015/7/28.
 */
class DownCount(val worksNum: Int, var successNum: Int)

object DownMaster {
  var sequence_id = 0

  case class DownFile(fileUrl: String, file: String)

  case class WorkerDownSectionSuccess(downFileInfo: DownFileInfo)


  def storeWorkFile(fileTempOS: String, startIndex: Int, buffer: Array[Byte]) = synchronized {
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}

case class Download(downFileInfo: DownFileInfo)

case class RequertGetFile(downFileInfo: DownFileInfo)

class DownMaster(downManagerActorRef: ActorRef) extends Actor with ActorLogging {

  import DownMaster._

  //在启动时下载为下载完的部分

  //  Await.result(db.run(downFileWorkList.filter(_.success === 0).result).map(_.foreach {
  //    case (file, fileUrl, startIndex, enIndex, success, restartCount) =>
  //      val worker = context.watch(context.actorOf(Props(new DownWorker(fileUrl, 1, startIndex, enIndex, file, self)), name = "work" + getSequence))
  //      worker ! Downming
  //  }), Duration.Inf)


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 50, withinTimeRange = 60 seconds) {
    case _: Exception => {
      Restart
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor:" + self.path + ", preRestart parent, reason:" + reason + ", message:" + message)
  }

  override def postRestart(reason: Throwable) {
    log.debug("actor:{}, postRestart parent, reason:{}", self.path, reason)
  }

  override def receive: Receive = {
    case Download(downFileInfo) =>

      allocationWorker(downFileInfo)

    case WorkerDownSectionSuccess(downFileInfo) =>

      val fileurl = downFileInfo.fileUrl
      deleteDownWorker(fileurl)
      deleteDownfile(fileurl)

      renameFile(downFileInfo)

      downManagerActorRef ! DownFileSuccess(downFileInfo)

    case Terminated(actorRef) =>
      println(actorRef.path.name + "被中置")

  }

  def allocationWorker(downFileInfo: DownFileInfo): Unit = {

    val fileTmpOS = downFileInfo.fileTempOS
    val fileUrl = downFileInfo.fileUrl
    val fileLength = downFileInfo.fileLength
    val downWokerAmount = downFileInfo.workerNumber
    val file = downFileInfo.file

    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, downWokerAmount)
    log.info("定位在下文件{}...", fileTmpOS)

    //创建临时文件需要的目录和文件
    downFileInfo.createTmpfile
    log.info("临时文件创建完毕")

    insertDownMaster(file, fileUrl, downWokerAmount)

    for (i <- 1 to downWokerAmount) {
      val startIndex = downFileInfo.workerDownInfo(i)._1
      val endIndex = downFileInfo.workerDownInfo(i)._2
      insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
      context.watch(context.actorOf(Props(new DownWorker(self)))) ! WorkerDownSelfSection(downFileInfo, i)
      log.debug("线程: {} 下载请求已经发送...", i)
    }
  }

  def renameFile(downFileInfo: DownFileInfo): Unit = {
    val fileOS = downFileInfo.fileOS
    val fileTempOS = downFileInfo.fileTempOS
    val fileOSHeadle = new File(fileOS);
    val fileTempOSHeadle = new File(fileTempOS);
    fileTempOSHeadle.renameTo(fileOSHeadle)
  }
}
