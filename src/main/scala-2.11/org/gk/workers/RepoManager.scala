package org.gk.workers

import java.io.File

import akka.actor.{Actor, Props}
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
      val file = downFileInfo.file
      val fileOS = downFileInfo.fileOS

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      decodeFileLocalExists(fileOS) match {
        case true => {
          log.info("文件:{} 存在本地,准备返回给请求者...",file)
          context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(downFileInfo)
        }
        case false => {
          log.info("文件:{} 不在本地...",file)
          downManager ! RequertDownFile(downFileInfo)
        }
      }
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalExists(fileOs:String): Boolean = {
    val osFileHandle = new File(fileOs)
    osFileHandle.exists()
  }
}
