package org.gk.server.workers.down

import java.io.RandomAccessFile

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.server.config.cfg
import org.gk.server.db.DML._
import org.gk.server.workers.DownFileInfo
import org.gk.server.workers.DownFileInfo
import org.gk.server.workers.down.DownManager.DownFileSuccess
import org.gk.server.workers.down.DownWorker.WorkerDownSelfSection

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/28.
 */
class DownCount(val worksNum: Int, var successNum: Int)

object DownMaster {

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

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 50, withinTimeRange = 60 seconds) {
    case _: Exception => {
      Restart
    }
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

      allocationWorker(downFileInfo)

    case WorkerDownSectionSuccess(downFileInfo) =>

      val fileurl = downFileInfo.fileUrl
      deleteDownWorker(fileurl)
      deleteDownfile(fileurl)

      downFileInfo.renameFile

      downManagerActorRef ! DownFileSuccess(downFileInfo)

    case Terminated(actorRef) =>
      println(actorRef.path.name + "被中置")
  }

  def allocationWorker(downFileInfo: DownFileInfo): Unit = {

    val file = downFileInfo.file
    val fileUrl = downFileInfo.fileUrl
    val fileLength = downFileInfo.fileLength
    val downWokerAmount = downFileInfo.workerNumber
    val fileTmpOS = downFileInfo.fileTempOS

    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, downWokerAmount)
    log.info("定位在下文件{}...", fileTmpOS)

    //创建临时文件需要的目录和文件
    downFileInfo.createTmpfile
    log.info("临时文件创建完毕")



    for (i <- 1 to downWokerAmount) {
      val startIndex = downFileInfo.workerDownInfo(i)._1
      val endIndex = downFileInfo.workerDownInfo(i)._2
      insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
      context.watch(context.actorOf(Props(new DownWorker(self)))) ! WorkerDownSelfSection(downFileInfo, i)
      log.debug("线程: {} 下载请求已经发送...", i)
    }
  }
}