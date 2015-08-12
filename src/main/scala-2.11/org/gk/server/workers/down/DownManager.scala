package org.gk.server.workers.down

import java.net.Socket

import akka.actor.{ActorRef, Actor, Props}
import org.gk.server.db.DML
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables._
import org.gk.server.workers.RepoManager.RequertFile
import org.gk.server.workers.{DownFileInfo, ActorRefWorkerGroups}
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

  case class DownFileSuccess(filePath:String)

  var requertDowingFileMap: Map[String, ArrayBuffer[DownFileInfo]] = Map.empty

  object DB {
    def add(filePath: String, value: DownFileInfo) = {
      val downfileInfoArrayBuffer = new ArrayBuffer[DownFileInfo]()
      downfileInfoArrayBuffer += value
      requertDowingFileMap += (filePath -> downfileInfoArrayBuffer)
    }
  }

}

import org.gk.server.workers.down.DownManager._

class DownManager extends Actor with akka.actor.ActorLogging {


  override def receive: Actor.Receive = {

    case RequertDownFile(downFileInfo) =>
      val fileOS = downFileInfo.fileOS
      val filePath = downFileInfo.filePath
      val repoName = downFileInfo.repoName

      if (!requertDowingFileMap.contains(filePath)) {
        DB.add(filePath, downFileInfo)
        val repoEnabledCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).filter(_.start === true).length.result), Duration.Inf)
        if (repoEnabledCount > 0) {

          context.watch(context.actorOf(Props[DownMaster])) ! Download(downFileInfo)
        } else {
          val repoDisableCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).length.result), Duration.Inf)
          if (repoDisableCount > 0) println("仓库" + repoName + "存在,但没有开启") else println("仓库不存在")
        }
      } else {
        println("文件正在下载...,等待下载完成返回")
        val socketArrayBuffer = requertDowingFileMap(filePath)
        socketArrayBuffer += downFileInfo
      }

    case DownFileSuccess(filePath) =>
      val downMasterActorRef = sender()
      context.unwatch(downMasterActorRef)
      context.stop(downMasterActorRef)
      requertDowingFileMap.filter(_._1 == filePath).map(p => {
        p._2.foreach(l => {
          ActorRefWorkerGroups.repoManager ! RequertFile(l)
        })
      })
      requertDowingFileMap -=(filePath)
  }
}