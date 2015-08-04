package org.gk.workers.down

import java.io._
import java.net.{Socket, HttpURLConnection, URL}

import akka.actor.{ActorRef, Props, Actor}
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.workers.down.DownManager.{RequertDownRepo, RequertDownFile}
import org.gk.workers.down.DownMaster.DownFile
import org.gk.workers.down.RepoSearcher.{ SearchPepo}

/**
 * Created by goku on 2015/7/22.
 */
object DownManager {
  case class RequertDownRepo(file:String)
  case class RequertDownFile(fileUrl:String,file:String)
}
class DownManager extends Actor with akka.actor.ActorLogging{

  val repoSearcher = context.actorOf(Props[RepoSearcher],name ="repoSearcher")
  val downMaster = context.actorOf(Props[DownMaster],name ="downMaster")

  var downSuccessNumber:Int = _
  var repoManagerActor:ActorRef = _
  override def receive: Actor.Receive = {
    case RequertDownRepo(file) =>
      repoSearcher ! SearchPepo(file)

    case RequertDownFile(fileUrl,file) =>
      downMaster ! DownFile(fileUrl,file)

    case ("FileDownSuccess",fileOS:String) =>
//      repoManager ! ("DownSuccess",fileOS)

  }
}

//case class Work(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOs:String)



