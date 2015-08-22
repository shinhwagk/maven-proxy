package org.gk.maven

import java.io._

import akka.actor.Actor
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import org.gk.server.workers.ActorRefWorkerGroups
import slick.driver.H2Driver.api._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.net.Socket
/**
 * Created by gk on 15/7/26.
 */

object RepoManager {
  var fileCache: Map[String, Array[Byte]] = Map.empty
}

class RepoManager extends Actor with akka.actor.ActorLogging {

  override def receive: Receive = {

    case (socket:Socket,filePath: String) =>

      filePath match {
        case _ if RepoManager.fileCache.contains(filePath) =>
          ActorRefWorkerGroups.returner ! (socket,filePath)
        case _ =>
          val fileOS = "aa" + filePath
          val fileOSHeadle = new File(fileOS)

          if (fileOSHeadle.exists()) {
            val buffer = new Array[Byte](fileOSHeadle.length().toInt)
            new FileInputStream(fileOSHeadle).read(buffer)
            RepoManager.fileCache += (filePath -> buffer)
            self ! (socket,filePath)
          } else {
            ActorRefWorkerGroups.downManager ! (getFileUrl(filePath),fileOS)
          }
      }
  }

  def getFileUrl(filePath: String): String = {
    val repoName = filePath.split("/")(1)
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    filePath.replace("/" + repoName + "/", repoUrl + "/")
  }
}