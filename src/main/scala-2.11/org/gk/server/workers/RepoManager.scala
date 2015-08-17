package org.gk.server.workers

import java.io.File
import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.server.config.cfg
import org.gk.server.workers.down.DownManager.RequertDownFile

/**
 * Created by gk on 15/7/26.
 */
object RepoManager {

  case class RequertFile(socket:Socket)

}

class RepoManager extends Actor with akka.actor.ActorLogging {

  import RepoManager._

  override def receive: Receive = {

    case RequertFile(socket) =>

      val headers = new Headers(socket)
      val filePath = headers.Head_Path.get
      val fileOS = cfg.getLocalMainDir + filePath

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      decodeFileLocalRepoExists(fileOS) match {
        case true =>
          log.info("文件:{} 存在本地,准备返回给请求者...", fileOS)

          context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(headers)

        case false =>
          ActorRefWorkerGroups.downManager ! RequertDownFile(headers)
      }
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalRepoExists(fileOS: String): Boolean = {
    println("检测文件是否存在")
    val fileHeadle = new File(fileOS)
    fileHeadle.exists()
  }
}