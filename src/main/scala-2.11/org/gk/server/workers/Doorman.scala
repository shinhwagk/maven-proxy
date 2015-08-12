package org.gk.server.workers

import java.net.Socket

import akka.actor.{Actor, ActorRef}
import org.gk.server.config.cfg
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by goku on 2015/7/24.
 */

class Doorman extends Actor {

  override def receive: Receive = {
    case socket: Socket =>
      ActorRefWorkerGroups.headParser ! RequertParserHead(DownFileInfo(socket))
  }
}

case class DownFileInfo(s: Socket) {

  val socket: Socket = s

  lazy val repoName: String = filePath.split("/")(1)

  var repoUrl: String = _

  var headInfo: Map[String, String] = _

  lazy val filePath: String = headInfo("PATH")

  lazy val fileUrl: String = getFileUrl

  lazy val fileOS: String = cfg.getLocalMainDir + filePath

  var fileLength: Int = _

  lazy val workerNumber: Int = getDownWorkerNumber

  lazy val workerDownInfo: Map[Int, (Int, Int, Array[Byte])] = getWokerDownRangeInfo

  private def getDownWorkerNumber: Int = {
    val processForBytes = cfg.getPerProcessForBytes
    if (fileLength >= processForBytes) fileLength / processForBytes else 1
  }

  private def getFileUrl: String = {
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    filePath.replace("/" + repoName + "/", repoUrl + "/")
  }

  private def getWokerDownRangeInfo: Map[Int, (Int, Int, Array[Byte])] = {
    val endLength = fileLength % workerNumber
    val step = (fileLength - endLength) / workerNumber
    var tempMap: Map[Int, (Int, Int, Array[Byte])] = Map.empty
    for (i <- 1 to workerNumber) {
      val startIndex: Int = (i - 1) * step
      val endIndex = if (i == workerNumber) i * step + endLength - 1 else i * step - 1
      tempMap += (i ->(startIndex, endIndex, new Array[Byte](endIndex - startIndex + 1)))
    }
    tempMap
  }
}

