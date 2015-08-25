package org.gk.download

import java.io.{FileOutputStream, File, RandomAccessFile}
import java.net.{HttpURLConnection, URL}

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.download.DownManager.{DownSuccess, DownFailure}
import org.gk.download.DownMaster.{Download, WorkerDownSectionSuccess}
import org.gk.download.DownWorker.WorkerDownSelfSection

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/28.
 */
object DownMaster {

  case class WorkerDownSectionSuccess(workerNumber: Int, Buffer: Array[Byte])

  case class Download()

}

class DownMaster(downManagerActorRef: ActorRef, fileUrl: String, fileOS: Option[String]) extends Actor with ActorLogging {

  val httpConn = new URL(fileUrl).openConnection.asInstanceOf[HttpURLConnection]
  httpConn.setConnectTimeout(10000)
  httpConn.setReadTimeout(10000)
  httpConn.setRequestProperty("Cache-Control", "no-cache")
  httpConn.setRequestProperty("Cache-store", "no-store");
  httpConn.setRequestProperty("Expires", "0")
  httpConn.setRequestProperty("Pragma", "no-cache")
  httpConn.setRequestProperty("Range", "bytes=0-1")



  var workerSuccessCount: Int = _
  val workerAmount: Int = Runtime.getRuntime.availableProcessors() * 3
  val fileBuffer = new Array[ArrayBuffer[Byte]](workerAmount)
  var downSuccessCount: Int = _
  var downSuccessSectionBufferMap: Map[Int, Array[Byte]] = Map.empty

  override def receive: Receive = {
    case Download =>
      httpConn.getResponseCode match {
        case 206 =>
          val fileLength = httpConn.getHeaderField("Content-Range").split("/")(1).toInt
          val workerAlgorithm = httpConn.getHeaderField("Content-Length").toInt
          workerAlgorithm match {
            case 2 =>
              workerDown01(fileLength)
            case 1 =>
              workerDown02(fileLength)
          }
        case 404 =>
          println("sssssssssss")
          downManagerActorRef ! DownFailure(fileUrl, httpConn.getResponseCode)
        case _ =>
          downManagerActorRef ! DownFailure(fileUrl, httpConn.getResponseCode)
      }

    case WorkerDownSectionSuccess(workerNumber, fileSectionBuffer) =>
      workerSuccessCount += 1
      downSuccessSectionBufferMap += (workerNumber -> fileSectionBuffer)
      println("下载完成----:" + workerSuccessCount + "/" + workerAmount)
      if (workerSuccessCount == workerAmount) {
        val fileBuffer = new ArrayBuffer[Byte]()
        downSuccessSectionBufferMap.toList.sortBy(_._1).map(l => {
          val buffer = l._2
          fileBuffer ++= buffer
        })
        val buffer = fileBuffer.toArray
        log.info("文件:{}.下载完毕", fileOS)
        println(fileOS)
        if (fileOS != None) storeWorkFile
        downManagerActorRef ! DownSuccess(fileUrl, buffer)
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


  def storeWorkFile = {
    val fileHeadle = new File(fileOS.get)
    if (!fileHeadle.getParentFile.exists()) fileHeadle.getParentFile.mkdirs()

    val fops = new FileOutputStream(fileHeadle);
    val fileBuffer = new ArrayBuffer[Byte]()
    downSuccessSectionBufferMap.toList.sortBy(_._1).map(l => {
      fileBuffer ++= l._2
    })
    fops.write(fileBuffer.toArray)
    fops.close()
  }

  def workerDown01(fileLength: Int) = {
    (1 to workerAmount).foreach(p => {
      val endLength = fileLength % workerAmount
      val step = (fileLength - endLength) / workerAmount
      val startIndex = (p - 1) * step
      val endIndex = if (p != workerAmount) (p) * step - 1 else (p) * step - 1 + endLength
      context.actorOf(Props(new DownWorker(self, p))) ! WorkerDownSelfSection(fileUrl, startIndex, endIndex)
    })
  }

  def workerDown02(fileLength: Int) = {
    (1 to workerAmount).foreach(p => {
      val endLength = fileLength % workerAmount
      val step = (fileLength - endLength) / workerAmount
      val startIndex = (p - 1) * step
      val endIndex = if (p != workerAmount) (p) * step else (p) * step + endLength
      context.actorOf(Props(new DownWorker(self, p))) ! WorkerDownSelfSection(fileUrl, startIndex, endIndex)
    })
  }
}