package org.gk.workers

import java.io.{File, RandomAccessFile}
import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/24.
 */


class Doorman extends Actor {
  val terminator = context.actorOf(Props[Terminator])
  val headParser = context.actorOf(Props[HeadParser], name = "HeadParser")

  //在启动时下载为下载完的部分

  //  Await.result(db.run(downFileWorkList.filter(_.success === 0).result).map(_.foreach {
  //    case (file, fileUrl, startIndex, enIndex, success, restartCount) =>
  //      val worker = context.watch(context.actorOf(Props(new DownWorker(fileUrl, 1, startIndex, enIndex, file, self)), name = "work" + getSequence))
  //      worker ! Downming
  //  }), Duration.Inf)

  override def receive: Receive = {
    case socket: Socket => {
      val downFileInfo = DownFileInfo(socket)
      headParser ! RequertParserHead(downFileInfo)
      println("requert发送者接受到请求，准备处理...")
      println("requert发送者发出请求...")

    }
  }
}

case class DownFileInfo(s: Socket) {

  val socket: Socket = s

  var headInfo: Map[String, String] = _

  lazy val file: String = headInfo("PATH")

  var fileUrl: String = _

  lazy val fileOS: String = cfg.getLocalRepoDir + file

  lazy val fileTempOS: String = fileOS + ".DownTmp"

  var fileLength: Int = _

  lazy val workerNumber: Int = getDownWokerNumber

  lazy val workerDownInfo: Map[Int, (Int, Int)] = getwokerDownInfo

  private def getDownWokerNumber: Int = {
    val processForBytes = cfg.getPerProcessForBytes
    if (fileLength >= processForBytes) fileLength / processForBytes else 1
  }

  def createTmpfile: Unit = {
    val file = new File(fileTempOS)

    if (!file.getParentFile.exists) file.getParentFile.mkdirs()

    if (!file.exists) {
      val raf = new RandomAccessFile(fileTempOS, "rwd");
      println("createTempfile" + fileTempOS + "长度:" + fileLength)
      raf.setLength(fileLength);
      raf.close()
    }
  }

  def getwokerDownInfo: Map[Int, (Int, Int)] = {
    val endLength = fileLength % workerNumber
    val step = (fileLength - endLength) / workerNumber
    var tempMap: Map[Int, (Int, Int)] = Map.empty
    for (i <- 1 to workerNumber) {
      val startIndex: Int = (i - 1) * step
      val endIndex = if (i == workerNumber) i * step + endLength else i * step - 1
      tempMap += (i ->(startIndex, endIndex))
    }
    tempMap
  }

  def renameFile: Unit = {
    val fileOSHeadle = new File(fileOS);
    val fileTempOSHeadle = new File(fileTempOS);
    fileTempOSHeadle.renameTo(fileOSHeadle)
  }
}

