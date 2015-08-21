package org.gk.server.workers.down

import java.io.{BufferedWriter, File, OutputStreamWriter, RandomAccessFile}
import java.net.{InetSocketAddress, Socket, URL}

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.server.config.cfg
import org.gk.server.workers.Anteroom.LeaveAnteroom
import org.gk.server.workers.down.DownWorker.WorkerDownSelfSection
import org.gk.server.workers.{ActorRefWorkerGroups, RequestHeaders}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/28.
 */
object DownMaster {

  case class DownFile(fileUrl: String, file: String)

  case class WorkerDownSectionSuccess(workerNumber: Int, Buffer: Array[Byte])

  case class Download(requestHeader: List[String], fileUrl: String, fileOS: String)

}

class DownMaster extends Actor with ActorLogging {

  import DownMaster._

  var workerSuccessCount: Int = _
  var fileUrl: String = _
  var workerAmount: Int = _
  var downSuccessCount: Int = _
  var downSuccessSectionBufferMap: Map[Int, Array[Byte]] = Map.empty
  var server: String = _
  var fileOS: String = _

  override def receive: Receive = {
    case Download(headerList, fileUrl, fileOS) =>

      this.fileUrl = fileUrl
      this.fileOS = fileOS
      val url = new URL(fileUrl);
      val host = url.getHost();
      val responseSocket = new Socket();
      val address = new InetSocketAddress(host, 80);
      responseSocket.connect(address);

      val bufferedWriter = new BufferedWriter(new OutputStreamWriter(responseSocket.getOutputStream(), "UTF8"));

      headerList.foreach(p => {
        p match {
          case _ if p.startsWith("GET") =>
            bufferedWriter.write("GET " + url.getFile + " " + p.split(" ")(2) + "\r\n")
          case _ if p.startsWith("HEAD") =>
            bufferedWriter.write("HEAD " + url.getFile + " " + p.split(" ")(2) + "\r\n")
          case _ if p.startsWith("Host") =>
            bufferedWriter.write("Host: " + host + "\r\n");
          case _ =>
            bufferedWriter.write(p + "\r\n")
        }
      })
      bufferedWriter.write("\r\n");
      bufferedWriter.flush()
      val aa = new RequestHeaders(responseSocket)
      server = "Nexus"
      println(aa.headText)

      aa.Head_HttpResponseCode.toInt match {
        case 404 =>
          ActorRefWorkerGroups.anteroom ! LeaveAnteroom(fileOS)
        case 200 =>
          val fileUrlLength = aa.Head_ContentLength.get.toInt
          workerAmount = getDownWorkerNumber(fileUrlLength)
          startWorkerDown(fileUrlLength)
      }

    case WorkerDownSectionSuccess(workerNumber, fileSectionBuffer) =>
      workerSuccessCount += 1
      downSuccessSectionBufferMap += (workerNumber -> fileSectionBuffer)
      println("下载完成----:" + workerSuccessCount + "/" + workerAmount)
      if (workerSuccessCount == workerAmount) {
        log.info("文件:{}.下载完毕", fileOS)
        storeWorkFile(fileOS)
        ActorRefWorkerGroups.anteroom ! LeaveAnteroom(fileOS)
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

  def startWorkerDown(fileUrlLength:Int): Unit = {
    //    log.info("待下载文件{},需要下载 {},需要线程数量{}...", fileUrl, fileLength, downWokerAmount)
    for (i <- 1 to workerAmount) {
      val endLength = fileUrlLength % workerAmount
      val step = (fileUrlLength - endLength) / workerAmount
      val startIndex: Int = (i - 1) * step

      val endIndex = if (server.startsWith("Nexus")) {
        if (i == workerAmount) i * step + endLength else i * step
      } else {
        if (i == workerAmount) i * step + endLength - 1 else i * step - 1
      }

//      println(startIndex + "~" + endIndex + "/" + workerAmount + "/" + fileUrlLength)
      context.watch(context.actorOf(Props(new DownWorker(self)))) ! WorkerDownSelfSection(i, fileUrl, startIndex, endIndex)
      log.debug("线程: {} 下载请求已经发送...", i)
    }
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


  private def getDownWorkerNumber(fileUrlLength:Int): Int = {
    val processForBytes = cfg.getPerProcessForBytes
//    println(fileUrlLength + "xxx" + processForBytes)
    if (fileUrlLength >= processForBytes) fileUrlLength / processForBytes else 1
  }
}