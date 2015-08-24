package org.gk.download

import java.io.{File, RandomAccessFile}
import java.net.{HttpURLConnection, URL}

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.download.DownMaster.{Download, WorkerDownSectionSuccess}
import org.gk.download.DownWorker.WorkerDownSelfSection
import org.gk.server.workers.ActorRefWorkerGroups
import org.gk.server.workers.Anteroom.LeaveAnteroom

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/28.
 */
object DownMaster {

  case class WorkerDownSectionSuccess(workerNumber: Int, Buffer: Array[Byte])

  object Download

}

class DownMaster(downManagerActorRef: ActorRef, filePath: String, fileURL: String, fileOS: Option[String]) extends Actor with ActorLogging {

  val httpConn = new URL(fileURL).openConnection.asInstanceOf[HttpURLConnection]
  httpConn.setConnectTimeout(2000)
  httpConn.setReadTimeout(2000)
  httpConn.setRequestMethod("HEAD")
  httpConn.setRequestProperty("Range", "bytes=0-1")

  var workerSuccessCount: Int = _
  val workerAmount: Int = Runtime.getRuntime.availableProcessors() * 2
  val fileBuffer = new Array[ArrayBuffer[Byte]](workerAmount)
  var downSuccessCount: Int = _
  var downSuccessSectionBufferMap: Map[Int, Array[Byte]] = Map.empty

  override def receive: Receive = {
    case Download =>
      httpConn.getResponseCode match {
        case 206 =>
          val fileLength = httpConn.getContentLength
          if (fileLength == 2) {
            (1 to workerAmount).foreach(p => {
              val fileWorkerBuffer = fileBuffer(p - 1)
              context.actorOf(Props(new DownWorker(self, workerAmount, p, fileURL, fileLength))) ! WorkerDownSelfSection(fileWorkerBuffer, 0)
            })
          }

        case _ =>
          println(httpConn.getResponseCode)
      }

    case WorkerDownSectionSuccess(workerNumber, fileSectionBuffer) =>
      workerSuccessCount += 1
      downSuccessSectionBufferMap += (workerNumber -> fileSectionBuffer)
      println("下载完成----:" + workerSuccessCount + "/" + workerAmount)
      if (workerSuccessCount == workerAmount) {
        log.info("文件:{}.下载完毕", fileOS)
        if (fileOS != None) {
          storeWorkFile(fileOS.get)
        }
        ActorRefWorkerGroups.anteroom ! LeaveAnteroom(fileOS.get)
      }
    case Terminated(actorRef) =>
      println(actorRef.path.name + "被中置")
  }


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 50, withinTimeRange = 60 seconds) {
    case _: Exception => Restart
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor:" + self.path + ", preRestart parent, reason:" + reason + ", message:" + message)
    self ! Download
  }


  def storeWorkFile(fileOS: String) = {

    val fileHeadleTemp = new File(fileOS + ".temp")
    val fileHeadle = new File(fileOS)
    if (!fileHeadle.getParentFile.exists()) {
      fileHeadle.getParentFile.mkdirs()
    }
    val raf = new RandomAccessFile(fileOS + ".temp", "rwd")
    val fileBuffer = new ArrayBuffer[Byte]()
    downSuccessSectionBufferMap.toList.sortBy(_._1).map(l => {
      val buffer = l._2
      fileBuffer ++= buffer
    })
    val buffer = fileBuffer.toArray
    raf.write(buffer)
    raf.close()

    fileHeadleTemp.renameTo(fileHeadle)
    fileOS
  }

}