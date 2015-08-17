package org.gk.server.workers.down

import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables._
import org.gk.server.workers.Collectors.DBFileInsert
import org.gk.server.workers.down.DownMaster.Download
import org.gk.server.workers.{Headers, ActorRefWorkerGroups, Returner, RuntrunFile}
import slick.driver.H2Driver.api._
import scala.concurrent.duration._
import akka.util.Timeout
import akka.event.LoggingReceive
import akka.pattern.{ask, pipe}
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {

  case class RequertDownFile(headers: Headers)

  case class DownFileSuccess(headers:Headers)

}

import org.gk.server.workers.down.DownManager._

class DownManager extends Actor with akka.actor.ActorLogging {

  import context.dispatcher

  implicit val askTimeout = Timeout(5 seconds)

  override def receive: Actor.Receive = {

    case RequertDownFile(headers) =>
      val filePath = headers.Head_Path.get
      val repoName = filePath.split("/")(1)
      val socket = headers.socket

      val repoEnabledCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).filter(_.start === true).length.result), Duration.Inf)

      if (repoEnabledCount > 0) {
        ActorRefWorkerGroups.collectors ? DBFileInsert(filePath, socket) map {
          case "Ok" => Download(headers)
        } pipeTo context.watch(context.actorOf(Props[DownMaster]))
      } else {
        val repoDisableCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).length.result), Duration.Inf)
        if (repoDisableCount > 0) println("仓库" + repoName + "存在,但没有开启") else println("仓库不存在")
        ActorRefWorkerGroups.terminator !(404, socket)
      }


    case DownFileSuccess(headers) =>
      val downMasterActorRef = sender()
//      context.unwatch(downMasterActorRef)
//      context.stop(downMasterActorRef)
          context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(headers)
  }
}