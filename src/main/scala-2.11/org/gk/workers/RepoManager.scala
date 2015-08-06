package org.gk.workers

import java.io.{RandomAccessFile, File}

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import org.gk.config.cfg
import java.net.Socket

import org.gk.config.cfg._
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.RepoManager.{RuntrunFile, RequertReturnFile}
import org.gk.workers.down.DownManager
import org.gk.workers.down.DownManager.{SendFile, RequertDownFile}
import slick.dbio.DBIO

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.driver.H2Driver.api._
/**
 * Created by gk on 15/7/26.
 */
object RepoManager {
  case class RequertReturnFile(downFileInfoBeta2:DownFileInfoBeta2)
  case class RuntrunFile(fileOS:String,socket:Socket)
}

class RepoManager extends Actor with akka.actor.ActorLogging{

  val retrunFile = context.actorOf(Props[Returner],name ="RetrunFile")
  val downManager = context.actorOf(Props(new DownManager(self)), name = "DownManager")

  override def receive: Receive = {
    case RequertReturnFile(downFileInfoBeta2) =>
      val file = downFileInfoBeta2.file
      val socket = downFileInfoBeta2.socket
      val fileOS = downFileInfoBeta2.fileOS

      /**
       * 判断文件是否已经缓存在本地仓库
       */
      decodeFileLocalExists(fileOS) match {
        case true => {
          log.info("文件:{} 存在本地,准备返回给请求者...",file)
          retrunFile ! RuntrunFile(fileOS,socket)
        }
        case false => {
          log.info("文件:{} 不在本地...",file)
          downManager ! RequertDownFile(downFileInfoBeta2)
        }
      }

    case SendFile(fileOS:String) =>{
//      retrunFile ! RuntrunFile(fileOS,socket)
    }
  }

  //查看文件是否存在本地仓库
  def decodeFileLocalExists(fileOs:String): Boolean = {
    val osFileHandle = new File(fileOs)
    osFileHandle.exists()
  }
}
