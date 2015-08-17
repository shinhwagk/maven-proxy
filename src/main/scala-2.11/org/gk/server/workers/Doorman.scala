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


  object DB {
    private var requertFileMap: Map[String, ArrayBuffer[Socket]] = Map.empty

    def create(filePath: String, value: Socket) = synchronized {
      val socketArrayBuffer = new ArrayBuffer[Socket]()
      socketArrayBuffer += value
      requertFileMap += (filePath -> socketArrayBuffer)
    }

    def insert(filePath: String, value: Socket) = synchronized {
      val socketArrayBuffer = requertFileMap(filePath)
      socketArrayBuffer += value
    }

    def delete(filePath: String) = synchronized {
      requertFileMap -= (filePath)
    }

    def getTable: Map[String, ArrayBuffer[Socket]] = synchronized {
      requertFileMap
    }
  }

}


//检查数据库
class Doorman extends Actor with ActorLogging{

  implicit val askTimeout = Timeout(5 seconds)

  override def receive: Receive = {
    case socket: Socket =>
      ActorRefWorkerGroups.repoManager ! RequertFile(socket)
  }
}


