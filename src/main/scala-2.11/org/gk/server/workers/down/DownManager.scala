package org.gk.server.workers.down

import java.net.Socket

import akka.actor.{Actor, ActorRef, Props}
import org.gk.server.db.DML
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables._
import org.gk.server.workers.DownFileInfo
import org.gk.server.workers.RepoManager.RequertFile
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

  var requertDowingFileMap: Map[Socket, String] = Map.empty

}

import org.gk.server.workers.down.DownManager._

class DownManager(repoManagerActorRef: ActorRef) extends Actor with akka.actor.ActorLogging {

  //  val downMasterActor = context.actorOf(Props(new DownMaster(self)), name = "DownMaster")
  //  context.system.scheduler.schedule(Duration.Zero, 3 second, repoManagerActorRef, RequertFile(downFileInfo))
  override def receive: Actor.Receive = {

    case RequertDownFile(downFileInfo) =>
      //      tuneRepoActorRef

      val socket = downFileInfo.socket
      val fileOS = downFileInfo.fileOS
      val repoName = downFileInfo.repoName

      if (checkFileDecodeDownning(fileOS)) {
        val repoEnabledCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).filter(_.start === true).length.result), Duration.Inf)
        if (repoEnabledCount > 0) {
          val fileURL = downFileInfo.fileUrl
          val downWokerAmount = downFileInfo.workerNumber
          DML.insertDownMaster(fileOS, fileURL, downWokerAmount)
          context.watch(context.actorOf(Props(new DownMaster(self)))) ! Download(downFileInfo)
        } else {
          val repoDisableCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).length.result), Duration.Inf)
          if (repoDisableCount > 0) println("仓库" + repoName + "存在,但没有开启") else println("仓库不存在")
        }
      } else {
        requertDowingFileMap += (socket -> fileOS)
        //        DownManager.requertSameDowingFileMap += (downFileInfo.socket -> downFileInfo)
      }

    case DownFileSuccess(downFileInfo) =>
      //      val downMasterActorRef = sender()
      //      context.unwatch(downMasterActorRef)
      //      context.unwatch(downMasterActorRef)
      //      for((k,v) <- requertDowingFileMap) {
      ////        repoManagerActorRef ! RequertFile(k,v) //需要修改错误
      //      }
      repoManagerActorRef ! RequertFile(downFileInfo)
  }

  def checkFileDecodeDownning(fileOS: String): Boolean = {
    import org.gk.server.db.MetaData._
    import org.gk.server.db.Tables._
    val count = Await.result(db.run(downFileList.filter(_.fileOS === fileOS).length.result), Duration.Inf)
    if (count == 0) true else false
  }
}

//case class Work(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOs:String)




