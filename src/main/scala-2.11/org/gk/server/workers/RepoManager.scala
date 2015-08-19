package org.gk.server.workers

import java.io.File

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gk.server.config.cfg
import org.gk.server.workers.Collectors.JoinFileDownRequestSet
import org.gk.server.workers.down.DownManager.RequertDownFile

import scala.concurrent.duration._

/**
 * Created by gk on 15/7/26.
 */
object RepoManager {

  case class RequertFile(requestHeader: RequestHeader)

}

class RepoManager extends Actor with akka.actor.ActorLogging {

  import RepoManager._

  implicit val askTimeout = Timeout(5 seconds)

  import context.dispatcher

  override def receive: Receive = {

    case RequertFile(requestHeader) =>

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      if (decodeFileLocalRepoExists(requestHeader.filePath))
        context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(requestHeader.socket, requestHeader.filePath)
      else
        ActorRefWorkerGroups.collectors ? JoinFileDownRequestSet(cfg.getLocalMainDir + requestHeader.filePath, requestHeader.socket) map {
          case "Ok" => {
            RequertDownFile(requestHeader)
          }
        } pipeTo ActorRefWorkerGroups.downManager
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalRepoExists(filePath: String): Boolean = new File(cfg.getLocalMainDir + filePath).exists()
}