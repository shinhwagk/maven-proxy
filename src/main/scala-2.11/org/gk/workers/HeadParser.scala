package org.gk.workers

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import akka.actor.{Props, Actor}
import org.gk.db.Tables._
import org.gk.workers.RepoManager.RequertReturnFile
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
class HeadParser extends Actor with akka.actor.ActorLogging{

  val repoManager = context.actorOf(Props[RepoManager],name = "repoManager")

  import org.gk.db.MetaData._




  override def receive: Receive = {
    case socket:Socket =>{
      log.info("headParser收到请求....")
//      val file  = getFile(socket)
      val headBuffers = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val headFirstLine = headBuffers.readLine()
      val file = headFirstLine.split(" ")(1)

      log.info("headParser解析出需要下载的文件:{}....",file)
      log.info("headParser发送请求给RepoManager")
      repoManager ! RequertReturnFile(file,socket)
    }
  }

  //从头信息获得下载文件
  def getFile(socket:Socket): String ={
    val headBuffers = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val headFirstLine = headBuffers.readLine()
    headFirstLine.split(" ")(1)
  }
}