package org.gk.download

import java.io.{BufferedInputStream, RandomAccessFile}
import java.net.{HttpURLConnection, URL}

import akka.actor.{Actor, ActorLogging, _}
import org.gk.download.DownMaster.WorkerDownSectionSuccess
import org.gk.download.DownWorker.WorkerDownSelfSection

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/28.
 */

object DownWorker {

  case object Downming

  case class WorkerDownSelfSection(fileUrl: String, startIndex: Int, endIndex: Int)

  def storeWorkFile(fileTempOS: String, startIndex: Int, buffer: Array[Byte]) = synchronized {
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}

class DownWorker(downMasterActorRef: ActorRef, workerNumber: Int) extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case WorkerDownSelfSection(fileUrl, startIndex, endIndex) => {


      val httpConn = new URL(fileUrl).openConnection.asInstanceOf[HttpURLConnection]
      httpConn.setConnectTimeout(5000)
      httpConn.setReadTimeout(5000)
      httpConn.setRequestProperty("Range", s"bytes=$startIndex-$endIndex")
      val fileWorkerBuffer = ArrayBuffer[Int]()
      val bis = new BufferedInputStream(httpConn.getInputStream)
      val dividingLine = ArrayBuffer(-1) //\n\r
      while (fileWorkerBuffer.takeRight(1) != dividingLine) {
        fileWorkerBuffer += bis.read()
        println(workerNumber + "----" + startIndex + "------------" + endIndex + " -----" + httpConn.getContentLength + "////" + fileWorkerBuffer.length)
      }

      fileWorkerBuffer.trimEnd(1)
      downMasterActorRef ! WorkerDownSectionSuccess(workerNumber, fileWorkerBuffer.map(_.toByte).toArray)

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