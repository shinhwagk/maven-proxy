package org.gk.workers.down

import java.io._
import java.net.{Socket, HttpURLConnection, URL}
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL
import org.gk.db.DML._
import org.gk.workers.{DownFileInfoBeta3, DownFileInfoBeta2}
import slick.dbio.DBIO

import scala.concurrent.duration._
import akka.actor.{ActorRef, Props, Actor}
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.down.DownManager.{DownLoadFile, SendFile, RequertDownFile}
import org.gk.workers.down.DownMaster.DownFile
import org.gk.workers.down.RepoSearcher.{SearchPepo}
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.H2Driver.api._
import scala.concurrent.Await
import org.gk.config._
import org.gk.config.cfg._

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {

  case class RequertDownFile(downFileInfoBeta2: DownFileInfoBeta2)

  case class DownLoadFile(downFileInfoBeta3: DownFileInfoBeta3)

  case class SendFile(downFileInfoBeta3: DownFileInfoBeta3)

}

case class DownFileInfo(file: String, fileURL: String) {
  val fileOS: String = getLocalRepoDir + file

  val fileTempOS: String = getLocalRepoDir + file + ".DownTmp"

  val fileLength: Int = getDownFileLength(fileURL)

  val downWorkerNumber: Int = if (fileLength >= cfg.getPerProcessForBytes) fileLength / cfg.getPerProcessForBytes else 1

  Await.result(db.run(DBIO.seq(downFileList +=(file, fileURL, downWorkerNumber))), Duration.Inf)

  private def getDownFileLength(fileURL: String): Int = {
    import java.net.{HttpURLConnection, URL};
    val conn = new URL(fileURL).openConnection().asInstanceOf[HttpURLConnection];
    conn.setConnectTimeout(5000)
    conn.setReadTimeout(5000)
    val fileLength = conn.getContentLength
    conn.disconnect()
    fileLength
  }

}

class DownManager(repoManagerActor: ActorRef) extends Actor with akka.actor.ActorLogging {

  val repoSearcherActor = context.actorOf(Props[RepoSearcher], name = "RepoSearcher")
  val downMasterActor = context.actorOf(Props[DownMaster], name = "DownMaster")


  var repoManager: ActorRef = _

  override def receive: Actor.Receive = {
    case RequertDownFile(downFileInfoBeta2) =>
      repoSearcherActor ! SearchPepo(downFileInfoBeta2)

    case DownLoadFile(downFileInfoBeta3) =>
      val fileURL = downFileInfoBeta3.fileURL
      val file = downFileInfoBeta3.file
      println(fileURL)
      if (checkFileDecodeDownning(file)) {
        downMasterActor ! Download(downFileInfoBeta3)
      } else {
        println("文件在下载，。。。")
      }

    case SendFile(downFileInfoBeta3) =>
      repoManagerActor ! ("DownSuccess", downFileInfoBeta3)
  }

  def checkFileDecodeDownning(file: String): Boolean = {
    val count = Await.result(db.run(downFileList.filter(_.file === file).length.result), Duration.Inf)
    if (count == 0) true else false
  }

  def fileTempOS(file: String): String = {
    getLocalRepoDir + file + ".DownTmp"
  }


  def getFileOSName(file: String): String = {
    getLocalRepoDir + file
  }
}

//case class Work(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOs:String)




