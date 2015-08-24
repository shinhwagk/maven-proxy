package org.gk.repository

import java.io._

import akka.actor.{Props, Actor}
import org.gk.download.DownManager
import org.gk.repository.RepoManager._
import org.gk.server.config.cfg
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import slick.driver.H2Driver.api._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.net.Socket

/**
 * Created by gk on 15/7/26.
 */

object RepoManager {
  var fileCache: Map[String, Array[Byte]] = Map.empty

  var anteroom: Map[String, ArrayBuffer[Socket]] = Map.empty

  case class SreachFile(socket: Socket, filePath: String)

  case class MemSreach(socket: Socket, filePath: String)

  case class DiskSreach(socket: Socket, filePath: String)

  case class DownFile(socket: Socket, filePath: String)

  case class DownFileSuccess(fileUrl: String, fileArrayByte: Array[Byte])

  case class DownFileFailure(fileUrl: String, code: Int)

}

class RepoManager extends Actor with akka.actor.ActorLogging {

  val downManager = context.actorOf(Props(new DownManager(self)), name = "RepoManager_DownManager")
  val returner = context.actorOf(Props[Returner], name = "Returner")
  override def receive: Receive = {
    case SreachFile(socket, filePath) =>
      self ! MemSreach(socket, filePath)

    case MemSreach(socket, filePath) =>
      if (RepoManager.fileCache.contains(filePath)) {
        println("cache中存在" + filePath + ".已经Cache:" + RepoManager.fileCache.size)
        returner !(socket, RepoManager.fileCache(filePath))
      }
      else
        self ! DiskSreach(socket, filePath)

    case DiskSreach(socket, filePath) =>
      val fileOS = cfg.getLocalMainDir + filePath
      println(fileOS)
      val fileOSHeadle = new File(fileOS)
      if (fileOSHeadle.exists()) {
        println("disk中存在" + filePath)
        val buffer = new Array[Byte](fileOSHeadle.length().toInt)
        new FileInputStream(fileOSHeadle).read(buffer)
        RepoManager.fileCache += (filePath -> buffer)
        self ! MemSreach(socket, filePath)
      } else {
        self ! DownFile(socket, filePath)
      }

    case DownFile(socket, filePath) =>
      val fileURL = getFileUrl(filePath)
      if (RepoManager.anteroom.contains(fileURL))
        RepoManager.anteroom(fileURL) += socket
      else
        RepoManager.anteroom += (filePath -> ArrayBuffer(socket))
      println("下载" + filePath)
    //      downManager !(filePath, getFileUrl(filePath), Some(fileOS))

    case DownFileSuccess(filePath, fileArrayByte) =>
      RepoManager.fileCache += (filePath -> fileArrayByte)
      RepoManager.anteroom(filePath).foreach(self ! MemSreach(_, filePath))
      RepoManager.anteroom -= (filePath)

    case DownFileFailure(filePath, code) =>
      //      RepoManager.anteroom(filePath).foreach(p => ActorRefWorkerGroups.terminator !(code, p))
      RepoManager.anteroom -= (filePath)
  }

  def getFileUrl(filePath: String): String = {
    val repoName = filePath.split("/")(1)
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    filePath.replace("/" + repoName + "/", repoUrl + "/")
  }
}