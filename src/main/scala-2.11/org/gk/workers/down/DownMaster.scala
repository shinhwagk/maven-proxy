package org.gk.workers.down

import java.io.{File, RandomAccessFile}
import java.net.HttpURLConnection

import akka.actor.SupervisorStrategy.Restart
import akka.actor._
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._
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

  case class WorkDownSuccess(url: String, file: String, startIndex: Int)

  def getSequence: Int = synchronized {
    sequence_id += 1
    sequence_id
  }
}

case class RequertDownFile(downFileInfo: DownFileInfo)

class DownMaster extends Actor with ActorLogging {

  import DownMaster._

  var downManager: ActorRef = _

  //在启动时下载为下载完的部分

  Await.result(db.run(downFileWorkList.filter(_.success === 0).result).map(_.foreach {
    case (file, fileUrl, startIndex, enIndex, success) =>
      val worker = context.watch(context.actorOf(Props(new DownWorker(fileUrl, 1, startIndex, enIndex, file)), name = "work" + getSequence))
      worker ! Downming
  }), Duration.Inf)


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 150 seconds) {
    case _: Exception => Restart
  }

  override def receive: Receive = {
    case RequertDownFile(downFileInfo) =>
      context.setReceiveTimeout(15 seconds)
      downManager = sender()
      allocationWork(downFileInfo)

    case WorkDownSuccess(fileurl, file, startIndex) => {
      Await.result(db.run(downFileWorkList.filter(_.fileUrl === fileurl).filter(_.startIndex === startIndex).map(p => (p.success)).update(1)), Duration.Inf)

      Await.result(db.run(downFileWorkList.filter(_.fileUrl === fileurl).map(p => (p.success)).result), Duration.Inf).toList.sum

      closeActorRef(sender())

      import org.gk.db.DML._
      val wokerSuccessNumber = countDownSuccessNumber(fileurl)
      val fileDownNumber = selectDownNumber(fileurl)
      println("查看已经完成数量++++++++" + wokerSuccessNumber + "/" + fileDownNumber)
      if (wokerSuccessNumber == fileDownNumber) {
        deleteDownWorker(fileurl)
        deleteDownfile(fileurl)
        val fileOS = cfg.getLocalRepoDir + file
        val fileTempOS = fileOS + ".DownTmp"


        println("下载完成啦")
        println(fileOS)
        println(fileTempOS)
        val fileOSHeadle = new File(fileOS);
        val fileTempOSHeadle = new File(fileTempOS);
        fileTempOSHeadle.renameTo(fileOSHeadle)
      }
    }
    case Terminated(actorRef) => {
      println(actorRef.path.name + "被中置")
    }
  }

  def allocationWork(downFileInfo: DownFileInfo): Unit = {

    val fileTmpOS = downFileInfo.fileTempOS
    val fileLength = downFileInfo.fileLength

    val fileUrl = downFileInfo.fileURL
    val workNumber = getWorkNum(fileLength)
    val file = downFileInfo.file

    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, workNumber)
    log.info("定位在下文件{}...", fileTmpOS)

    //创建临时文件需要的目录和文件
    createTmpfile(fileTmpOS, fileLength)

    val endLength = fileLength % workNumber

    //步长
    val step = (fileLength - endLength) / workNumber

    val downaa = new DownCount(workNumber, 0)



    for (thread <- 1 to workNumber) {
      thread match {
        case _ if thread == workNumber =>
          val startIndex = (thread - 1) * step
          val endIndex = thread * step + endLength
          insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl, thread, startIndex, endIndex, file)))) ! Downming
        //          log.info("线程: {} 下载请求已经发送...",thread)

        case _ =>
          val startIndex = (thread - 1) * step
          val endIndex = thread * step + endLength - 1
          insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl, thread, startIndex, endIndex, file)))) ! Downming
        //          log.info("线程: {} 下载请求已经发送...",thread)


      }
    }
  }


  def getWorkNum(fileLength: Int): Int = {
    val processForBytes = cfg.getPerProcessForBytes
    if (fileLength >= processForBytes) fileLength / processForBytes else 1
  }

  def createTmpfile(fileTmpOS: String, fileLength: Int): Unit = {
    val file = new File(fileTmpOS)

    if (!file.getParentFile.exists()) {
      file.getParentFile.mkdirs()
    }

    val raf = new RandomAccessFile(fileTmpOS, "rwd");
    raf.setLength(fileLength);
    raf.close()

    log.info("创建临时文件{}...", fileTmpOS)
  }

  def renameFile(fileTemp: String): Unit = {

  }

  def closeActorRef(actor: ActorRef): Unit = {
    context.unwatch(actor)
    context.stop(actor)
  }
}
