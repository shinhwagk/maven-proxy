package org.gk.workers

import java.io.{RandomAccessFile, File}

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import org.gk.config.cfg
import java.net.Socket

import org.gk.log.GkConsoleLogger
import org.gk.workers.down.DownManager

import scala.collection.mutable.ArrayBuffer

/**
 * Created by gk on 15/7/26.
 */
class RepoManager extends Actor with akka.actor.ActorLogging{
  val senderr = context.actorOf(Props[Sender],name ="Sender")
  val terminator = context.actorOf(Props[Terminator],name = "terminator")
  val downLoader = context.actorOf(Props(new DownManager(self)), name = "downLoader")
  var socket:Socket = _
  override def receive: Receive = {
    case (file:String,socket:Socket) =>{
      this.socket = socket
      val fileOs = cfg.getLocalRepoDir + file
      val osFileHandle = new File(fileOs)
      osFileHandle.exists() match {
        case true => {
          log.debug("文件:{} 存在本地...",fileOs)
          senderr ! ("SenderRequert",fileOs,this.socket)
        }
        case false => {
          log.debug("文件:{} 不在本地...",fileOs)
//          downLoader ! (getFileUrl(file),fileOs,socket)
          downLoader ! ("DownFileRequest",file)
//          getFile(file,socket)
        }
      }
    }
    case ("DownSuccess",fileOS:String) =>{
      senderr ! ("SenderRequert",fileOS,socket)
    }
  }
}
