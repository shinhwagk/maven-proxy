package org.gk.workers

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import akka.actor.{Props, Actor}
import org.gk.db.Tables._
import org.gk.workers.RepoManager.RequertFile
import org.gk.workers.down.DownWorker
import org.gk.workers.down.DownWorker.Downming
import slick.driver.H2Driver.api._
import slick.dbio.DBIO
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import scala.concurrent.Await

/**
 * Created by goku on 2015/7/27.
 */

case class RequertParserHead(downFileInfo: DownFileInfo)

class HeadParser extends Actor with akka.actor.ActorLogging {

  val repoManager = context.actorOf(Props[RepoManager], name = "RepoManager")

  override def receive: Receive = {
    case RequertParserHead(downFileInfo) => {
      log.info("headParser收到请求....")
      downFileInfo.headInfo = getHeadInfo(downFileInfo.socket)
      val file = downFileInfo.file
      log.info("headParser解析出需要下载的文件:{}....", file)
      log.info("headParser发送请求给RepoManager")

      repoManager ! RequertFile(downFileInfo)
    }
  }


  def getHeadInfo(socket: Socket): Map[String, String] = {
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    var a: Map[String, String] = Map.empty
    var templine = br.readLine()
    val b = templine.split(" ")

    a += ("PATH" -> b(1))

    templine = br.readLine()

    while (templine != null && templine != "") {
      println(templine)
      val b = templine.split(":")

      b(0) match {
        case "Connection" => a += (b(0) -> b(1))
        case _ => None
      }
      templine = br.readLine()
    }
    a
  }
}