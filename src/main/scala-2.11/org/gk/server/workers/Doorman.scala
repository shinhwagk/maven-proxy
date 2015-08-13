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

    def delete (filePath: String) = {
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

//case class DownFileInfo(s: Socket) {
//
//  val socket: Socket = s
//
//  lazy val repoName: String = filePath.split("/")(1)
//
//  var repoUrl: String = _
//
//  var headInfo: Map[String, String] = _
//
//  lazy val filePath: String = headInfo("PATH")
//
//  lazy val fileUrl: String = getFileUrl
//
//  lazy val fileOS: String = cfg.getLocalMainDir + filePath
//
//  var fileLength: Int = _
//
//  lazy val workerNumber: Int = getDownWorkerNumber
//
//  lazy val workerDownInfoworkerDownInfo: Map[Int, (Int, Int, Array[Byte])] = getWorkerDownRangeInfo
//
//  private def getDownWorkerNumber: Int = {
//    val processForBytes = cfg.getPerProcessForBytes
//    if (fileLength >= processForBytes) fileLength / processForBytes else 1
//  }
//
//  private def getFileUrl: String = {
//    val repoUrl = Await.result(db.run(Tables.repositoryTable.filter(_.name === repoName).map(_.url).result), Duration.Inf).head
//    filePath.replace("/" + repoName + "/", repoUrl + "/")
//  }
//
//  private def getWorkerDownRangeInfo: Map[Int, (Int, Int, Array[Byte])] = {
//    val endLength = fileLength % workerNumber
//    val step = (fileLength - endLength) / workerNumber
//    var tempMap: Map[Int, (Int, Int, Array[Byte])] = Map.empty
//    for (i <- 1 to workerNumber) {
//      val startIndex: Int = (i - 1) * step
//      val endIndex = if (i == workerNumber) i * step + endLength - 1 else i * step - 1
//      tempMap += (i ->(startIndex, endIndex, new Array[Byte](endIndex - startIndex + 1)))
//    }
//    tempMap
//  }
//}

