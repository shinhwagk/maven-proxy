package org.gk.workers.down

import java.net.Socket

import akka.actor.{Actor, ActorRef, Props}
import org.gk.config.cfg
import org.gk.config.cfg._
import org.gk.db.DML._
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

  var requertDowingFileMap:Map[Socket,String] = Map.empty

}

import DownManager._
class DownManager(repoManagerActorRef: ActorRef) extends Actor with akka.actor.ActorLogging {

  val downMasterActor = context.actorOf(Props(new DownMaster(self)), name = "DownMaster")
//  context.system.scheduler.schedule(Duration.Zero, 3 second, repoManagerActorRef, RequertFile(downFileInfo))
  override def receive: Actor.Receive = {

    case RequertDownFile(downFileInfo) =>

      val socket = downFileInfo.socket
      val fileOS = downFileInfo.fileOS
      val fileUrl = downFileInfo.fileUrl
      val downWokerAmount = downFileInfo.workerNumber

      if (checkFileDecodeDownning(fileOS)) {
        insertDownMaster(fileOS, fileUrl, downWokerAmount)
        context.watch(context.actorOf(Props(new DownMaster(self)))) ! Download(downFileInfo)
      } else {
        requertDowingFileMap += (socket -> fileOS)
//        DownManager.requertSameDowingFileMap += (downFileInfo.socket -> downFileInfo)
      }

    case DownFileSuccess(downFileInfo) =>
      val downMasterActorRef = sender()
      context.unwatch(downMasterActorRef)
      context.unwatch(downMasterActorRef)
      for((k,v) <- requertDowingFileMap) {
//        repoManagerActorRef ! RequertFile(k,v) //需要修改错误
      }
      repoManagerActorRef ! RequertFile(downFileInfo)
  }

  def checkFileDecodeDownning(fileOS: String): Boolean = {
    val count = Await.result(db.run(downFileList.filter(_.fileOS === fileOS).length.result), Duration.Inf)
    if (count == 0) true else false
  }
}

//case class Work(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOs:String)




