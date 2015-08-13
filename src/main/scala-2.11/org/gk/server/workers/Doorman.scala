package org.gk.server.workers

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import akka.actor.Actor
import org.gk.server.workers.HeadParser.RequertFilePath
import org.gk.server.workers.RepoManager.RequertFile

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/24.
 */
object Doorman {

  case class StoreRequert(filePath: String, socket: Socket)


  object DB {
    var requertFileMap: Map[String, ArrayBuffer[Socket]] = Map.empty

    def create(filePath: String, value: Socket) = {
      val socketArrayBuffer = new ArrayBuffer[Socket]()
      socketArrayBuffer += value
      requertFileMap += (filePath -> socketArrayBuffer)
    }

    def insert(filePath: String, value: Socket) = {
      val socketArrayBuffer = requertFileMap(filePath)
      socketArrayBuffer += value
    }

    def delete(filePath: String) = {
      requertFileMap -= (filePath)
    }
  }

}

class Doorman extends Actor {

  import Doorman._

  override def receive: Receive = {
    case socket: Socket =>
      ActorRefWorkerGroups.headParser ! RequertFilePath(socket)

    case StoreRequert(filePath: String, socket: Socket) =>
      DB.requertFileMap.contains(filePath) match {
        case true =>
          DB.insert(filePath, socket)
        case false =>
          DB.create(filePath, socket)
          ActorRefWorkerGroups.repoManager ! RequertFile(filePath)
      }
  }

  def getFilePath(socket: Socket): String = {
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val templine = br.readLine()
    templine.split(" ")(1)
  }
}


