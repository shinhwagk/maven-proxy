package org.gk.workers

import java.io.File

import akka.actor.{Props, Actor}
import org.gk.workers.RepoManager.{RequertFile}
import org.gk.workers.down.DownManager
import org.gk.workers.down.DownManager.RequertDownFile

/**
 * Created by gk on 15/7/26.
 */
object RepoManager {
  case class RequertReturnFile(downFileInfoBeta2:DownFileInfoBeta2)

  case class RequertFile(downFileInfo:DownFileInfo)
//  case class RuntrunFile(downFileInfoBeta3:DownFileInfoBeta3)
}

class RepoManager extends Actor with akka.actor.ActorLogging{

  val returner = context.actorOf(Props[Returner],name ="Returner")
  val downManager = context.actorOf(Props(new DownManager(self)), name = "DownManager")

  override def receive: Receive = {
    case RequertFile(downFileInfo) =>
      val file = downFileInfo.file
      val socket = downFileInfo.socket
      val fileOS = downFileInfo.fileOS

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      decodeFileLocalExists(fileOS) match {
        case true => {
          log.info("文件:{} 存在本地,准备返回给请求者...",file)
          returner ! RuntrunFile(downFileInfo)
        }
        case false => {
          log.info("文件:{} 不在本地...",file)
          downManager ! RequertDownFile(downFileInfo)
        }
      }

//    case ("DownSuccess", downFileInfoBeta3:DownFileInfoBeta3) =>{
//      retrunFile ! RuntrunFile(downFileInfoBeta3)
//    }
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalExists(fileOs:String): Boolean = {
    val osFileHandle = new File(fileOs)
    osFileHandle.exists()
  }
}
