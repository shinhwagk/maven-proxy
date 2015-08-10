package org.gk.workers

import java.io.File

import akka.actor.{Actor, Props}
import org.gk.config.cfg
import org.gk.workers.RepoManager.RequertFile
import org.gk.workers.down.DownManager
import org.gk.workers.down.DownManager.RequertDownFile

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
      val socket = downFileInfo.socket

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