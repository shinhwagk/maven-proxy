package org.gk.server.workers

import java.io.File
import java.net.Socket

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.gk.server.FileWriterTest
import org.gk.server.config.cfg
import org.gk.server.workers.Collectors.DBFileInsert
import org.gk.server.workers.down.DownManager.RequertDownFile

import scala.collection.mutable.ArrayBuffer
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
      println("33")
      val filePath = requestHeader.filePath
      println("33")
      val socket = requestHeader.socket
      val fileOS = cfg.getLocalMainDir + filePath
      println(fileOS)
      /**
       * 判断文件是否已经缓存在本地仓库
       */
      decodeFileLocalRepoExists(fileOS) match {
        case true =>
          log.info("文件:{} 存在本地,准备返回给请求者...", fileOS)

          context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(socket, fileOS)

        case false =>
//          ActorRefWorkerGroups.collectors ? DBFileInsert(filePath, socket) map {
//            case "Ok" => {
//              println("xxx")
//              RequertDownFile(requestHeader)
//            }
//          } pipeTo
            ActorRefWorkerGroups.downManager ! RequertDownFile(requestHeader)
      }
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalRepoExists(fileOS: String): Boolean = {
    println("检测文件是否存在")
    val fileHeadle = new File(fileOS)
    fileHeadle.exists()
  }


}