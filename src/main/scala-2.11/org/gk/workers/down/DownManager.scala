package org.gk.workers.down

import akka.actor.{Actor, ActorRef, Props}
import org.gk.config.cfg._
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.DownFileInfo
import org.gk.workers.RepoManager.RequertFile
import org.gk.workers.down.DownManager.{DownFileSuccess, RequertDownFile}
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {

  case class RequertDownFile(downFileInfo: DownFileInfo)

  case class DownLoadFile(downFileInfo: DownFileInfo)

  case class DownFileSuccess(downFileInfo: DownFileInfo)

}

class DownManager(repoManagerActorRef: ActorRef) extends Actor with akka.actor.ActorLogging {

  val downMasterActor = context.actorOf(Props(new DownMaster(self)), name = "DownMaster")

  override def receive: Actor.Receive = {
    //    case RequertDownFile(downFileInfo) =>
    //      log.info("请求仓库....")
    //      context.watch(context.actorOf(Props(new RepoSearcher(self)))) ! RequertFileUrl(downFileInfo)
    ////      repoSearcher ! RequertFileUrl(downFileInfo)

    case RequertDownFile(downFileInfo) =>
      //      val repoSearcherActorRef = sender()
      //      context.unwatch(repoSearcherActorRef)
      //      context.stop(repoSearcherActorRef)
      val fileURL = downFileInfo.fileUrl
      val file = downFileInfo.file
      println(fileURL+"xxx")
      if (checkFileDecodeDownning(file)) {
        context.watch(context.actorOf(Props(new DownMaster(self)))) ! Download(downFileInfo)
      } else {
        println("文件在下载，。。。")
      }

    case DownFileSuccess(downFileInfo) =>
      val downMasterActorRef = sender()
      context.unwatch(downMasterActorRef)
      context.unwatch(downMasterActorRef)
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




