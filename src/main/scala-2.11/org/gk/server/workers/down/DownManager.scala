package org.gk.server.workers.down

import akka.actor.{Actor, Props}
import akka.util.Timeout
import org.gk.server.config.cfg
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import org.gk.server.db.Tables._
import org.gk.server.tool.RequestHeader
import org.gk.server.workers._
import org.gk.server.workers.down.DownManager.RequertDownFile
import org.gk.server.workers.down.DownMaster.Download
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {

  case class RequertDownFile(requestHeader: RequestHeader)

}

class DownManager extends Actor with akka.actor.ActorLogging {

  override def receive: Actor.Receive = {

    case RequertDownFile(requestHeader) =>

      val repoName = requestHeader.filePath.split("/")(1)
      val fileUrl = getFileUrl(requestHeader.filePath)
      val fileOS = cfg.getLocalMainDir + requestHeader.filePath

      val repoEnabledCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).filter(_.start === true).length.result), Duration.Inf)

      if (repoEnabledCount > 0)
        context.watch(context.actorOf(Props[DownMaster])) ! Download(requestHeader.headerList, fileUrl, fileOS)
      else {
        val repoDisableCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).length.result), Duration.Inf)
        if (repoDisableCount > 0) println("仓库" + repoName + "存在,但没有开启") else println("仓库不存在")
        ActorRefWorkerGroups.terminator !(404, requestHeader.socket)
      }
  }

  private def getFileUrl(filePath: String): String = {
    val repoName = filePath.split("/")(1)
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    filePath.replace("/" + repoName + "/", repoUrl + "/")
  }
}