package org.gk.workers.down

import java.io._
import java.net.{Socket, HttpURLConnection, URL}
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL
import org.gk.db.DML._
import org.gk.workers.RepoManager.RequertFile
import org.gk.workers.DownFileInfo
import slick.dbio.DBIO

import scala.concurrent.duration._
import akka.actor.{ActorRef, Props, Actor}
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.down.DownManager.{DownLoadFile, DownFileSuccess, RequertDownFile}
import org.gk.workers.down.DownMaster.DownFile
import org.gk.workers.down.RepoSearcher.{RequertFileUrl}
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.H2Driver.api._
import scala.concurrent.Await
import org.gk.config._
import org.gk.config.cfg._

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {

  case class RequertDownFile(downFileInfo: DownFileInfo)

  case class DownLoadFile(downFileInfo: DownFileInfo)

  case class DownFileSuccess(downFileInfo: DownFileInfo)

}

class DownManager(repoManagerActorRef: ActorRef) extends Actor with akka.actor.ActorLogging {

  val repoSearcher = context.actorOf(Props[RepoSearcher], name = "RepoSearcher")
  val downMasterActor = context.actorOf(Props(new DownMaster(self)), name = "DownMaster")


  override def receive: Actor.Receive = {
    case RequertDownFile(downFileInfo) =>
      log.info("请求仓库....")
      repoSearcher ! RequertFileUrl(downFileInfo)

    case DownLoadFile(downFileInfo) =>
      val fileURL = downFileInfo.fileUrl
      val file = downFileInfo.file
      println(fileURL)
      if (checkFileDecodeDownning(file)) {
        downMasterActor ! Download(downFileInfo)
      } else {
        println("文件在下载，。。。")
      }

    case DownFileSuccess(downFileInfo) =>
      repoManagerActorRef ! RequertFile(downFileInfo)
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




