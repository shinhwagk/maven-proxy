package org.gk.server.workers

import java.io.File
import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.server.config.cfg
import org.gk.server.tool.RequestHeader
import org.gk.server.workers.Anteroom.JoinAnteroom
import org.gk.server.workers.Returner.RuntrunFile

/**
 * Created by gk on 15/7/26.
 */

class RepoManager extends Actor with akka.actor.ActorLogging {

  override def receive: Receive = {

    case socket:Socket =>
      val requestHeader = RequestHeader(socket)
      val fileOS = cfg.getLocalMainDir + requestHeader.filePath

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      println("判断文件: " + fileOS + "是否存在本地仓库")

      decodeFileLocalRepoExists(fileOS) match {
        case true =>
          context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(requestHeader.socket, fileOS)
        case false =>
          println("文件不存在，把请求加入到队列...")
          ActorRefWorkerGroups.anteroom ! JoinAnteroom(requestHeader)
      }

      //查看文件是否存在本地仓库
      def decodeFileLocalRepoExists(fileOS: String): Boolean = new File(fileOS).exists()
  }
}