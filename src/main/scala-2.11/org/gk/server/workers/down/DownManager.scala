package org.gk.server.workers.down

import java.net.Socket

import akka.actor.{ActorRef, Actor, Props}
import org.gk.server.db.DML
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables._
import org.gk.server.workers.RepoManager.RequertFile
import org.gk.server.workers.{DownFileInfo, ActorRefWokerGroups}
import slick.driver.H2Driver.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {

  case class RequertDownFile(downFileInfo: DownFileInfo)

  case class DownLoadFile(downFileInfo: DownFileInfo)

  case class DownFileSuccess(fileOS:String)

  var requertDowingFileMap: Map[String, ArrayBuffer[DownFileInfo]] = Map.empty

  object DB {
    def add(key: String, value: DownFileInfo) = {
      val downfileInfoArrayBuffer = new ArrayBuffer[DownFileInfo]()
      downfileInfoArrayBuffer += value
      requertDowingFileMap += (key -> downfileInfoArrayBuffer)
    }
  }

}

import org.gk.server.workers.down.DownManager._

class DownManager extends Actor with akka.actor.ActorLogging {


  override def receive: Actor.Receive = {

    case RequertDownFile(downFileInfo) =>
      val socket = downFileInfo.socket
      val fileOS = downFileInfo.fileOS
      val fileURL = downFileInfo.fileUrl
      val repoName = downFileInfo.repoName

      if (!requertDowingFileMap.contains(fileOS)) {
        DB.add(fileOS, downFileInfo)
        val repoEnabledCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).filter(_.start === true).length.result), Duration.Inf)
        if (repoEnabledCount > 0) {

          DML.insertDownMaster(fileOS, fileURL)
          context.watch(context.actorOf(Props(new DownMaster(self)))) ! Download(downFileInfo)
        } else {
          val repoDisableCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).length.result), Duration.Inf)
          if (repoDisableCount > 0) println("仓库" + repoName + "存在,但没有开启") else println("仓库不存在")
        }
      } else {
        println("文件正在下载")
        val socketArrayBuffer = requertDowingFileMap(fileOS)
        socketArrayBuffer += downFileInfo
      }

    case DownFileSuccess(downFileInfo) =>
      requertDowingFileMap.filter(_._1 == downFileInfo.fileOS).map(p => {
        p._2.foreach(l => {
          ActorRefWokerGroups.repoManager ! RequertFile(l)
        })
      })
  }

  def checkFileDecodeDownning(fileOS: String): Boolean = {
    import org.gk.server.db.MetaData._
    import org.gk.server.db.Tables._
    val count = Await.result(db.run(downFileList.filter(_.fileOS === fileOS).length.result), Duration.Inf)
    if (count == 0) true else false
  }


}