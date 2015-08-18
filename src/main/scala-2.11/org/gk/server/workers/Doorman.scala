package org.gk.server.workers

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import akka.actor.{ActorLogging, Actor}
import akka.util.Timeout
import org.gk.server.workers.RepoManager.RequertFile

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/**
 * Created by goku on 2015/7/24.
 */
object Doorman {

  case class StoreRequert(filePath: String, socket: Socket)

}


//检查数据库
class Doorman extends Actor with ActorLogging{


  override def receive: Receive = {
    case socket: Socket =>
      ActorRefWorkerGroups.repoManager ! RequertFile(socket)
  }
}


