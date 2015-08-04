package org.gk.workers

import java.io.{RandomAccessFile, File}

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import org.gk.config.cfg
import java.net.Socket

import org.gk.workers.RepoManager.{RuntrunFile, RequertReturnFile}
import org.gk.workers.down.DownManager
import org.gk.workers.down.DownManager.RequertDownRepo

import scala.collection.mutable.ArrayBuffer

/**
 * Created by gk on 15/7/26.
 */
object RepoManager {
  case class RequertReturnFile(file:String,socket:Socket)
  case class RuntrunFile(fileOS:String,socket:Socket)
}

class RepoManager extends Actor with akka.actor.ActorLogging{

  val retrunFile = context.actorOf(Props[RetrunFile],name ="retrunFile")
  val downManager = context.actorOf(Props[DownManager], name = "DownManager")

  override def receive: Receive = {
    case RequertReturnFile(file,socket) =>

      val fileOS = cfg.getLocalRepoDir + file

      /*
      判断文件是否已经缓存在本地仓库
       */
      decodeFileExists(fileOS) match {
        case true => {
          log.info("文件:{} 存在本地,准备返回给请求者...",file)
          retrunFile ! RuntrunFile(fileOS,socket)
        }
        case false => {
          log.info("文件:{} 不在本地...",file)
          downManager ! RequertDownRepo(file)
        }
      }

    case ("DownSuccess",fileOS:String) =>{
      retrunFile ! ("SenderRequert",fileOS)
    }
  }

  //查看文件是否存在本地仓库
  def decodeFileExists(fileOs:String): Boolean = {
    val osFileHandle = new File(fileOs)
    osFileHandle.exists()
  }
}
