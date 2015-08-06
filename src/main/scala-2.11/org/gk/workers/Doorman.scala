package org.gk.workers

import java.net.{ServerSocket, Socket}

import akka.actor.{ActorRef, Props, Actor}
import org.gk.config.cfg
import org.gk.config.cfg._
import org.gk.db.MetaData._
import org.gk.db.Tables._
import slick.dbio.DBIO

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.H2Driver.api._

/**
 * Created by goku on 2015/7/24.
 */


class Doorman extends Actor {
  val terminator = context.actorOf(Props[Terminator])
  val headParser = context.actorOf(Props[HeadParser], name = "HeadParser")

  override def receive: Receive = {
    case socket: Socket => {
      val downFileInfoBeta1 = DownFileInfoBeta1(socket)
      headParser ! downFileInfoBeta1
      println("requert发送者接受到请求，准备处理...")
      println("requert发送者发出请求...")

    }
  }
}

case class DownFileInfoBeta1(socket: Socket) {
  def getDownFileInfoBeta2(file: String) = DownFileInfoBeta2(socket, file)
}

case class DownFileInfoBeta2(socket: Socket, file: String) {

  val fileOS: String = cfg.getLocalRepoDir + file

  val fileTempOS: String = fileOS + ".DownTmp"

  def getDownFileInfoBeta3(fileURL: String) = DownFileInfoBeta3(file, socket, fileURL, fileOS, fileTempOS)
}

case class DownFileInfoBeta3(file: String, socket: Socket, fileURL: String, fileOS: String, fileTempOS: String)


