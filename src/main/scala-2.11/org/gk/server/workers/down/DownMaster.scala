package org.gk.server.workers.down

import java.io.{OutputStreamWriter, BufferedWriter}
import java.net.{InetSocketAddress, Socket, HttpURLConnection, URL}

import akka.actor.SupervisorStrategy._
import akka.actor._
import org.gk.server.config.cfg
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import org.gk.server.workers.{Headers, ActorRefWorkerGroups}
import org.gk.server.workers.down.DownManager.DownFileSuccess
import org.gk.server.workers.down.DownWorker.WorkerDownSelfSection
import slick.driver.H2Driver.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/28.
 */
object DownMaster {

  case class DownFile(fileUrl: String, file: String)

  case class WorkerDownSectionSuccess(workerNumber: Int, Buffer: Array[Byte])

  case class Download(headers: Headers)

}


class DownMaster extends Actor with ActorLogging {

  import DownMaster._

  var workerSuccessCount: Int = _
  var fileUrlLength: Int = _
  var fileUrl: String = _
  lazy val workerAmount: Int = getDownWorkerNumber
  var filePath: String = _
  var downSuccessCount: Int = _
  lazy val fileOS = cfg.getLocalMainDir + filePath
  var downSuccessSectionBufferMap: Map[Int, Array[Byte]] = Map.empty
  var server: String = _
  var headers:Headers = _

  override def receive: Receive = {
    case Download(headers) =>
      this.headers = headers
      filePath = headers.Head_Path.get
      println("进入下载")
      fileUrl = getFileUrl(filePath)
      //      val downUrl = new URL(fileUrl);
      //      val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
      //      val responseCode = downConn.getResponseCode
      //      log.info("测试下载地址:{}.ResponseCode", downUrl)
      //      println(downUrl)

      val url = new URL(fileUrl);
      val host = url.getHost();
      val socket = new Socket();
      val address = new InetSocketAddress(host, 80);
      socket.connect(address);
      val bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
      bufferedWriter.write("GET " + url.getFile() + " HTTP/1.1\r\n"); // 请求头信息发送结束标志
      bufferedWriter.write("ContentType: application/octet-stream\r\n"); // 请求头信息发送结束标志
      bufferedWriter.write("Host: " + host + "\r\n"); // 请求头信息发送结束标志
      bufferedWriter.write("\r\n"); // 请求头信息发送结束标志
      bufferedWriter.flush()
      val aa = new Headers(socket)
      val responseCode = aa.Head_HttpResponseCode.toInt
      println(aa.Head_HttpResponseCode + "werwerwerewe" + url)
      server = aa.Head_Server.get

      responseCode match {
        case 404 =>
          ActorRefWorkerGroups.terminator ! (404, headers.socket)
        case 200 =>

          fileUrlLength = aa.Head_ContentLength.get.toInt
          println("xxxxxxxxxx" + fileUrlLength)
          startWorkerDown
      }
      socket.close()

    case WorkerDownSectionSuccess(workerNumber, fileSectionBuffer) =>
      workerSuccessCount += 1
      downSuccessSectionBufferMap += (workerNumber -> fileSectionBuffer)
      println("下载完成----:" + workerSuccessCount + "/" + workerAmount)
      if (workerSuccessCount == workerAmount) {
        log.info("文件:{}.下载完毕", filePath)
        storeWorkFile
        ActorRefWorkerGroups.downManager ! DownFileSuccess(headers)
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

  def startWorkerDown: Unit = {
    println("xxxxxxxx1111" + workerAmount)
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

      println(startIndex + "~" + endIndex + "/" + workerAmount + "/" + fileUrlLength)
      context.watch(context.actorOf(Props(new DownWorker(self)))) ! WorkerDownSelfSection(i, fileUrl, startIndex, endIndex)
      log.debug("线程: {} 下载请求已经发送...", i)
    }
  }

  def storeWorkFile = {
    import java.io._
    val fileHeadleTemp = new File(fileOS+".temp")
    val fileHeadle = new File(fileOS)
    if (!fileHeadle.getParentFile.exists()) {
      fileHeadle.getParentFile.mkdirs()
    }
    val raf = new RandomAccessFile(fileOS+".temp", "rwd")
    val fileBuffer = new ArrayBuffer[Byte]()
    downSuccessSectionBufferMap.toList.sortBy(_._1).map(l => {
      val buffer = l._2
      fileBuffer ++= buffer
    })
    val buffer = fileBuffer.toArray
    raf.write(buffer)
    raf.close()

    fileHeadleTemp.renameTo(fileHeadle)
  }

  private def getFileUrl(filePath: String): String = {
    val repoName = filePath.split("/")(1)
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    filePath.replace("/" + repoName + "/", repoUrl + "/")
  }

  private def getDownWorkerNumber: Int = {
    val processForBytes = cfg.getPerProcessForBytes
    println(fileUrlLength + "xxx" + processForBytes)
    if (fileUrlLength >= processForBytes) fileUrlLength / processForBytes else 1
  }
}