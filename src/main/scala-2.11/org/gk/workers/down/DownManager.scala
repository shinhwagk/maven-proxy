package org.gk.workers.down

import java.io._
import java.net.{Socket, HttpURLConnection, URL}

import akka.actor.{ActorRef, Props, Actor}
import akka.routing.RoundRobinPool
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/22.
 */
class DownManager(repoManager:ActorRef) extends Actor with akka.actor.ActorLogging{
  val processNumber = cfg.getDownFilePorcessNumber
  val repoSearcher = context.actorOf(Props[RepoSearcher],name ="repoSearcher")
  val downMaster = context.actorOf(Props(new DownMaster(processNumber,self)),name ="downMaster")
  var downSuccessNumber:Int = _
  var repoManagerActor:ActorRef = _
  override def receive: Actor.Receive = {
    case ("DownFileRequest",file:String) => {
      repoSearcher ! file
    }
    case ("RepoSreachSuccess",fileUrl:String,file:String) =>{
      val fileOs = cfg.getLocalRepoDir + file
      downMaster ! ("DownloadFile",fileUrl,fileOs)
    }
//    case (fileUrl:String,fileOs:String,socket:Socket,repoManager:ActorRef) =>{
//      repoManagerActor = repoManager
//      downFile(fileUrl,fileOs)
//      sender() ! ("DownSuccess",fileOs,socket)
//    }
    case ("FileDownSuccess",fileOS:String) => {
      repoManager ! ("DownSuccess",fileOS)
    }
  }



}

case class Work(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOs:String)



