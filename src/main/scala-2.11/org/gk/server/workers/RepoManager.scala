package org.gk.server.workers

import java.io.File

import akka.actor.{ActorRef, Actor, Props}
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables._
import org.gk.server.db.{MetaData, Tables}
import org.gk.server.workers.RepoManager.RequertFile
import org.gk.server.workers.down.{DownMaster, DownManager}
import org.gk.server.workers.down.DownManager.RequertDownFile
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by gk on 15/7/26.
 */
object RepoManager {
  case class RequertFile(downFileInfo:DownFileInfo)

}

class RepoManager extends Actor with akka.actor.ActorLogging{

  val downManager = context.actorOf(Props(new DownManager(self)), name = "DownManager")

  override def receive: Receive = {

    case RequertFile(downFileInfo) =>

      val fileOS = downFileInfo.fileOS

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      decodeFileLocalRepoExists(downFileInfo) match {
        case true => {
          log.info("文件:{} 存在本地,准备返回给请求者...",fileOS)

          context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(downFileInfo)
        }
        case false => {
          log.info("文件:{} 不在本地...",fileOS)
          downManager ! RequertDownFile(downFileInfo)
        }
      }

    case "ffs" =>
      val a = sender()
      context.unwatch(a)
      context.stop(a)
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalRepoExists(downFileInfo:DownFileInfo): Boolean = {
    println("检测文件是否存在")
    val fileOS = downFileInfo.fileOS
    val fileHeadle = new File(fileOS)
    fileHeadle.exists()
  }


}