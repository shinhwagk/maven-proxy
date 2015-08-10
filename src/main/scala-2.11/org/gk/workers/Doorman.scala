package org.gk.workers

import java.io.{File, RandomAccessFile}
import java.net.{ServerSocket, HttpURLConnection, Socket}

import akka.actor.{ActorRef, Actor, Props}
import org.gk.config.cfg
import org.gk.workers.Doorman.StartRepoService

/**
 * Created by goku on 2015/7/24.
 */

object Doorman {

  case class StartRepoService(repoName: String, repoUrl: String, repoPort: Int, doormanActorRef: ActorRef)

}

class Doorman extends Actor {
  val headParser = context.actorOf(Props[HeadParser], name = "HeadParser")

  override def receive: Receive = {
    case StartRepoService(repoName, repoUrl, repoPort, doormanActorRef) => {
      println("仓库:" + repoName + "已经启动. 端口:" + repoPort + ". 地址 " + repoUrl)
      val ss = new ServerSocket(repoPort);
      while (true) {
        val socket = ss.accept();
        headParser ! RequertParserHead(DownFileInfo(socket, repoName, repoUrl, repoPort))
        println("仓库:" + repoName + ",收到请求")
      }
    }
  }
}

case class DownFileInfo(s: Socket,n:String,u:String,p:Int) {

  val socket: Socket = s

  val  repoName: String = n

  val repoUrl: String = u

  val port:Int = p

  var headInfo: Map[String, String] = _

  lazy val file: String = headInfo("PATH")

  lazy val fileUrl: String = repoUrl + file

  lazy val fileOS: String = cfg.getLocalMainDir + "/" +  repoName + file

  lazy val fileTempOS: String = cfg.getLocalMainDir + "/" +  repoName + file + ".DownTmp"

  lazy val fileLength: Int = getfileUrlLength

  lazy val workerNumber: Int = getDownWokerNumber

  lazy val workerDownInfo: Map[Int, (Int, Int)] = getWokerDownRangeInfo

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

  private def getWokerDownRangeInfo: Map[Int, (Int, Int)] = {
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

  private def getfileUrlLength: Int = {
    import java.net.{HttpURLConnection, URL};
    val downUrl = new URL(fileUrl)
    val conn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    conn.setConnectTimeout(20000)
    conn.setReadTimeout(30000)
    conn.getContentLength
  }
}

