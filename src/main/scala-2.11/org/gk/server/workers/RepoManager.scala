package org.gk.server.workers

import java.io.File

import akka.actor.{Actor, Props}
import org.gk.server.workers.RepoManager.RequertFile
import org.gk.server.workers.down.DownManager.RequertDownFile

/**
 * Created by gk on 15/7/26.
 */
object RepoManager {

  case class RequertFile(downFileInfo: DownFileInfo)

}

class RepoManager extends Actor with akka.actor.ActorLogging {

  override def receive: Receive = {

    case RequertFile(downFileInfo) =>

      val fileOS = downFileInfo.fileOS
      val repoName = downFileInfo.repoName

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      decodeFileLocalRepoExists(fileOS) match {
        case true =>
          log.info("文件:{} 存在本地,准备返回给请求者...", fileOS)

          context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(downFileInfo)

        case false =>
          log.info("文件:{} 不在本地...", fileOS)

          ActorRefWorkerGroups.downManager ! RequertDownFile(downFileInfo)
      }
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalRepoExists(fileOS: String): Boolean = {
    println("检测文件是否存在")
    val fileHeadle = new File(fileOS)
    fileHeadle.exists()
  }
}