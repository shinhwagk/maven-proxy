package org.gk.workers.down

import java.io.{File, RandomAccessFile}
import java.net.HttpURLConnection

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.{DownFileInfo, DownFileInfoBeta3}
import org.gk.workers.down.DownManager.SendFile
import org.gk.workers.down.DownWorker.Downming
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

  case class WorkDownSuccess(url: String, file: String, startIndex: Int ,downFileInfoBeta3 :DownFileInfoBeta3)

  def getSequence: Int = synchronized {
    sequence_id += 1
    sequence_id
  }

  def storeWorkFile(fileTempOS: String, startIndex: Int, buffer: Array[Byte]) = synchronized {
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}

case class Download(downFileInfo: DownFileInfo)

case class RequertGetFile(downFileInfo: DownFileInfo)

class DownMaster extends Actor with ActorLogging {

  import DownMaster._

  var downManager: ActorRef = _

  //在启动时下载为下载完的部分

  Await.result(db.run(downFileWorkList.filter(_.success === 0).result).map(_.foreach {
    case (file, fileUrl, startIndex, enIndex, success, restartCount) =>
      val worker = context.watch(context.actorOf(Props(new DownWorker(fileUrl, 1, startIndex, enIndex, file, self)), name = "work" + getSequence))
      worker ! Downming
  }), Duration.Inf)


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 50, withinTimeRange = 60 seconds) {
    case _: Exception => {
      Restart
    }
  }

  override def receive: Receive = {
    case Download(downFileInfo) =>
      downManager = sender()
      allocationWork(downFileInfo)

    case WorkDownSuccess(fileurl, file, startIndex,downFileInfoBeta3) => {
      //      Await.result(db.run(downFileWorkList.filter(_.fileUrl === fileurl).filter(_.startIndex === startIndex).map(p => (p.success)).update(1)), Duration.Inf)

      //      import org.gk.db.DML._
      //      val wokerSuccessNumber = countDownSuccessNumber(fileurl)
      //      val fileDownNumber = selectDownNumber(fileurl)
      //
      //
      //      println("查看已经完成数量++++++++" + wokerSuccessNumber + "/" + fileDownNumber)
      println("下载完成啦")
      val fileOS = cfg.getLocalRepoDir + file
      val fileTempOS = fileOS + ".DownTmp"

      //      if (wokerSuccessNumber == fileDownNumber) {
      deleteDownWorker(fileurl)
      deleteDownfile(fileurl)

      println(fileOS)
      println(fileTempOS)
      val fileOSHeadle = new File(fileOS);
      val fileTempOSHeadle = new File(fileTempOS);
      fileTempOSHeadle.renameTo(fileOSHeadle)
      downManager ! SendFile(downFileInfoBeta3)
      //      }
    }
    case Terminated(actorRef) => {
      println(actorRef.path.name + "被中置")
    }
  }

  def allocationWork(downFileInfo: DownFileInfo): Unit = {

    val fileTmpOS = downFileInfo.fileTempOS
    val fileUrl = downFileInfo.fileUrl
    val fileLength = downFileInfo.fileLength
    val downWokerNumber = downFileInfo.workerNumber
    val file = downFileInfo.file

    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, downWokerNumber)
    println("需要线程数量" + downWokerNumber)
    log.info("定位在下文件{}...", fileTmpOS)

    //创建临时文件需要的目录和文件
    downFileInfo.createTmpfile

    val endLength = fileLength % downWokerNumber

    //步长
    val step = (fileLength - endLength) / downWokerNumber

    val downaa = new DownCount(downWokerNumber, 0)


    insertDownMaster(file, fileUrl, downWokerNumber)
    for (thread <- 1 to downWokerNumber) {
      thread match {
        case _ if thread == downWokerNumber =>
          val startIndex = (thread - 1) * step
          val endIndex = thread * step + endLength
          insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl, thread, startIndex, endIndex, file, self , downFileInfoBeta3)))) ! Downming
        //          log.info("线程: {} 下载请求已经发送...",thread)

        case _ =>
          val startIndex = (thread - 1) * step
          val endIndex = thread * step + endLength - 1
          insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl, thread, startIndex, endIndex, file, self ,downFileInfoBeta3)))) ! Downming
        //          log.info("线程: {} 下载请求已经发送...",thread)

      }
    }
  }



  def renameFile(fileTemp: String): Unit = {

  }

  def closeActorRef(actor: ActorRef): Unit = {
    context.unwatch(actor)
    context.stop(actor)
  }


}
