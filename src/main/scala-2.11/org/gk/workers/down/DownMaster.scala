package org.gk.workers.down

import java.io.{File, RandomAccessFile}
import java.net.HttpURLConnection

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.DownFileInfoBeta3
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

  case class WorkDownSuccess(url: String, file: String, startIndex: Int)

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

case class Download(downFileInfoBeta3: DownFileInfoBeta3)

case class RequertGetFile(downFileInfo: DownFileInfo)

class DownMaster extends Actor with ActorLogging {

  var a = 0
  var b = 0

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
    case Download(downFileInfoBeta3) =>
      downManager = sender()
      allocationWork(downFileInfoBeta3)
    case "a" =>
      println("wanbi ")
      Thread.sleep(10000)

    case WorkDownSuccess(fileurl, file, startIndex) => {
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
      downManager ! SendFile(fileOS)
      //      }
    }
    case Terminated(actorRef) => {
      println(actorRef.path.name + "被中置")
    }
  }

  def allocationWork(downFileInfoBeta3: DownFileInfoBeta3): Unit = {

    val fileTmpOS = downFileInfoBeta3.fileTempOS
    val fileUrl = downFileInfoBeta3.fileURL
    val fileLength = getDownFileLength(fileUrl)


    val workNumber = getWorkNum(fileLength)
    val file = downFileInfoBeta3.file

    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, workNumber)
    println("需要线程数量" + workNumber)
    log.info("定位在下文件{}...", fileTmpOS)

    //创建临时文件需要的目录和文件
    createTmpfile(fileTmpOS, fileLength)

    val endLength = fileLength % workNumber

    //步长
    val step = (fileLength - endLength) / workNumber

    val downaa = new DownCount(workNumber, 0)


    insertDownMaster(file, fileUrl, workNumber)
    for (thread <- 1 to workNumber) {
      thread match {
        case _ if thread == workNumber =>
          val startIndex = (thread - 1) * step
          val endIndex = thread * step + endLength
          insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl, thread, startIndex, endIndex, file, self)))) ! Downming
        //          log.info("线程: {} 下载请求已经发送...",thread)

        case _ =>
          val startIndex = (thread - 1) * step
          val endIndex = thread * step + endLength - 1
          insertDownWorker(file, fileUrl, startIndex, endIndex, 0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl, thread, startIndex, endIndex, file, self)))) ! Downming
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

  private def getDownFileLength(fileURL: String): Int = {
    import java.net.{HttpURLConnection, URL};
    val conn = new URL(fileURL).openConnection().asInstanceOf[HttpURLConnection];
    conn.setConnectTimeout(10000)
    conn.setReadTimeout(5000)
    val fileLength = conn.getContentLength
    conn.disconnect()
    fileLength
  }
}
