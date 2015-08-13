package org.gk.server.workers.down

import akka.actor.{Actor, Props}
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables._
import org.gk.server.workers.{RuntrunFile, Returner, ActorRefWorkerGroups}
import org.gk.server.workers.Doorman.DB
import org.gk.server.workers.RepoManager.RequertFile
import org.gk.server.workers.down.DownMaster.Download
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {

  case class RequertDownFile(filePath: String)

  case class DownFileSuccess(filePath: String)

}

import org.gk.server.workers.down.DownManager._

class DownManager extends Actor with akka.actor.ActorLogging {


  override def receive: Actor.Receive = {

    case RequertDownFile(filePath) =>
      val repoName = filePath.split("/")(1)

      val repoEnabledCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).filter(_.start === true).length.result), Duration.Inf)
      if (repoEnabledCount > 0) {
        context.watch(context.actorOf(Props[DownMaster])) ! Download(filePath)
      } else {
        val repoDisableCount = Await.result(db.run(repositoryTable.filter(_.name === repoName).length.result), Duration.Inf)
        if (repoDisableCount > 0) println("仓库" + repoName + "存在,但没有开启") else println("仓库不存在")
        ActorRefWorkerGroups.terminator !(404, filePath)
        //        context.system.scheduler.schedule(Duration.Zero, 30 second, self, RequertDownFile(filePath))
      }


    case DownFileSuccess(filePath) =>
      val downMasterActorRef = sender()
      context.unwatch(downMasterActorRef)
      context.stop(downMasterActorRef)
      context.watch(context.actorOf(Props[Returner])) ! RuntrunFile(filePath)
  }
}