package org.gk.download

import java.io.{BufferedInputStream, BufferedWriter, OutputStreamWriter, RandomAccessFile}
import java.net.{HttpURLConnection, InetSocketAddress, Socket, URL}

import akka.actor.{Actor, ActorLogging, _}
import org.gk.download.DownWorker.WorkerDownSelfSection
import org.gk.server.workers.RequestHeaders

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/28.
 */

object DownWorker {

  case object Downming

  case class WorkerDownSelfSection(fileWorkerBuffer: ArrayBuffer[Byte], downSuccessNumber: Int)

  def storeWorkFile(fileTempOS: String, startIndex: Int, buffer: Array[Byte]) = synchronized {
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}

class DownWorker(downMasterActorRef: ActorRef, workerAmount: Int, workerNumber: Int, fileURL: String, fileLength: Int) extends Actor with ActorLogging {

  val endLength = fileLength % workerAmount
  val step = (fileLength - endLength) / workerAmount


  override def receive: Actor.Receive = {
    case WorkerDownSelfSection(fileWorkerBuffer, downSuccessAmount) => {

      val httpConn = new URL(fileURL).openConnection.asInstanceOf[HttpURLConnection]
      val startIndex = (workerNumber - 1) * step - downSuccessAmount

      if (workerAmount != workerNumber) {

        val endIndex = workerNumber * step

        httpConn.setRequestProperty(s"Range",s"bytes=$startIndex-$endIndex")
        val workerFilrLength = httpConn.getContentLength

        val bis = new BufferedInputStream(httpConn.getInputStream)
        while (fileWorkerBuffer.takeRight(1) != ArrayBuffer(-1)) {
          fileWorkerBuffer += bis.read().toByte
          downSuccessAmount += 1
        }
        fileWorkerBuffer.trimEnd(1)

        if ((endIndex - startIndex) != workerFilrLength) {
          fileWorkerBuffer.trimEnd(1)
        }
      }else{
        httpConn.setRequestProperty(s"Range",s"bytes=$startIndex-")
        val workerFilrLength = httpConn.getContentLength

        val bis = new BufferedInputStream(httpConn.getInputStream)
        while (fileWorkerBuffer.takeRight(1) != ArrayBuffer(-1)) {
          fileWorkerBuffer += bis.read().toByte
          downSuccessAmount += 1
        }
        fileWorkerBuffer.trimEnd(1)
      }
      //      log.debug("线程: {} 下载{};收到,开始下载...",workerNumber,fileURL)
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor:" + self.path + ", preRestart parent, reason:" + reason + ", message:" + message)

    self ! message.get.asInstanceOf[WorkerDownSelfSection]
  }

  override def postRestart(reason: Throwable) {
    log.debug("actor:{}, postRestart parent, reason:{}", self.path, reason)
  }
}