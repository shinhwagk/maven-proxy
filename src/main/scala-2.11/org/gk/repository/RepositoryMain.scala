package org.gk.repository

import java.io.{PrintWriter, File, FileInputStream}
import java.net.Socket

import akka.actor.{Actor, Props}
import org.gk.download.DownManager
import org.gk.download.DownManager.{DownFailure, DownSuccess}
import org.gk.repository.RepositoryMain._
import org.gk.server.config.cfg
import org.gk.server.db.MetaData._
import org.gk.server.db.Tables
import slick.driver.H2Driver.api._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by goku on 2015/8/24.
 */

object RepositoryMain {
  var fileCache: Map[String, Array[Byte]] = Map.empty

  var anteroom: Map[String, ArrayBuffer[Socket]] = Map.empty

  var fileInfo: Map[String, String] = Map.empty

  case class SreachFile(socket: Socket, filePath: String)

  case class MemSreach(socket: Socket, filePath: String)

  case class DiskSreach(socket: Socket, filePath: String)

  case class DownFile(socket: Socket, filePath: String)

  case class DownFileSuccess(fileUrl: String, fileArrayByte: Array[Byte])

  case class DownFileFailure(fileUrl: String, code: Int)

}

class RepositoryMain extends Actor {

  val downManager = context.actorOf(Props(new DownManager(self)), name = "DownManager")
  val returner = context.actorOf(Props[Returner], name = "Returner")

  override def receive: Receive = {
    case SreachFile(socket, filePath) =>
      self ! MemSreach(socket, filePath)

    case MemSreach(socket, filePath) =>
      if (RepositoryMain.fileCache.contains(filePath)) {
        println("cache中存在" + filePath + ".已经Cache:" + RepositoryMain.fileCache.size)
        returner !(socket, RepositoryMain.fileCache(filePath))
      }
      else
        self ! DiskSreach(socket, filePath)

    case DiskSreach(socket, filePath) =>
      val fileOS = cfg.getLocalMainDir + filePath
      val fileOSHeadle = new File(fileOS)
      if (fileOSHeadle.exists()) {
        println("disk中存在" + filePath)
        val buffer = new Array[Byte](fileOSHeadle.length().toInt)
        new FileInputStream(fileOSHeadle).read(buffer)
        RepositoryMain.fileCache += (filePath -> buffer)
        self ! MemSreach(socket, filePath)
      } else {
        self ! DownFile(socket, filePath)
      }

    case DownFile(socket, filePath) =>
      val fileURL = getFileUrl(filePath)
      val fileOS = cfg.getLocalMainDir + filePath
      println(fileOS + "ooooooooooooo")
      if (RepositoryMain.anteroom.contains(fileURL))
        RepositoryMain.anteroom(fileURL) += socket
      else
        RepositoryMain.anteroom += (filePath -> ArrayBuffer(socket))

      if (!RepositoryMain.fileInfo.contains(fileURL))
        RepositoryMain.fileInfo += (fileURL -> filePath)

      println("下载" + filePath)
      downManager !(getFileUrl(filePath), Some(fileOS))

    case DownSuccess(fileURL, fileArrayByte) =>
      val filePath = RepositoryMain.fileInfo(fileURL)
      RepositoryMain.fileCache += (filePath -> fileArrayByte)
      RepositoryMain.anteroom(filePath).foreach(self ! MemSreach(_, filePath))
      RepositoryMain.anteroom -= (filePath)

    case DownFailure(fileURL, code) =>
      if (code == 404) {
        val filePath = RepositoryMain.fileInfo(fileURL)
        RepositoryMain.anteroom(filePath).foreach(p => {
          val out = new PrintWriter(p.getOutputStream())
          out.println("HTTP/1.1 404 Not found"); //返回应答消息,并结束应答
          out.println(""); // 根据 HTTP 协议, 空行将结束头信息
          out.close();
          p.close()
        })
        RepositoryMain.anteroom -= (filePath)
      } else {
        println(code + "新的代码。。。。")
      }

  }

  def getFileUrl(filePath: String): String = {
    val repoName = filePath.split("/")(1)
    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
    filePath.replace("/" + repoName + "/", repoUrl + "/")
  }
}
