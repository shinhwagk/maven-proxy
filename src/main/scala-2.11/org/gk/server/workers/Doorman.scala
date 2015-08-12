package org.gk.server.workers

import java.io.{File, RandomAccessFile}
import java.net.{ServerSocket, Socket}

import akka.actor.{Actor, ActorRef, Props}
import org.gk.server.config.cfg
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import org.gk.server.workers.Doorman.StartRepoService
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by goku on 2015/7/24.
 */

object Doorman {

  case class StartRepoService(repoName: String, repoUrl: String, repoPort: Int, doormanActorRef: ActorRef)

}

class Doorman extends Actor {

  override def receive: Receive = {
    case StartRepoService(repoName, repoUrl, repoPort, doormanActorRef) => {
      println("仓库:" + repoName + "已经启动. 端口:" + repoPort + ". 地址 " + repoUrl)
      val ss = new ServerSocket(repoPort);
      while (true) {
        val socket = ss.accept();
        ActorRefWokerGroups.headParser ! RequertParserHead(DownFileInfo(socket))
        println("仓库:" + repoName + ",收到请求")
      }
    }
    case socket: Socket =>
      ActorRefWokerGroups.headParser ! RequertParserHead(DownFileInfo(socket))
  }
}

case class DownFileInfo(s: Socket) {

  val socket: Socket = s

  lazy val repoName: String = file.split("/")(1)

  var repoUrl: String = _

  var headInfo: Map[String, String] = _

  lazy val file: String = headInfo("PATH")

  lazy val fileUrl: String = getFileUrl

  lazy val fileOS: String = cfg.getLocalMainDir + file

  lazy val fileLength: Int = getfileUrlLength

  lazy val workerNumber: Int = getDownWokerNumber

  lazy val workerDownInfo: Map[Int, (Int, Int, Array[Byte])] = getWokerDownRangeInfo

  private def getDownWokerNumber: Int = {
    val processForBytes = cfg.getPerProcessForBytes
    if (fileLength >= processForBytes) fileLength / processForBytes else 1
  }

  private def getFileUrl: String = {
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    file.replace("/" + repoName + "/", repoUrl + "/")
  }

  private def getWokerDownRangeInfo: Map[Int, (Int, Int, Array[Byte])] = {
    val endLength = fileLength % workerNumber
    val step = (fileLength - endLength) / workerNumber
    var tempMap: Map[Int, (Int, Int, Array[Byte])] = Map.empty
    for (i <- 1 to workerNumber) {
      val startIndex: Int = (i - 1) * step
      val endIndex = if (i == workerNumber) i * step + endLength -1 else i * step - 1
      tempMap += (i ->(startIndex, endIndex, new Array[Byte](endIndex - startIndex + 1)))
    }
    tempMap
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

