package org.gk.server.workers

import java.net.Socket

import akka.actor.Actor
import org.gk.server.workers.Collectors.{DBFileCreate, DBFileDelete, DBFileInsert, FilePathSocketArray}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/8/17.
 */

object Collectors {

  case class DBFileInsert(filePath: String, value: Socket)

  case class DBFileCreate(filePath: String, value: Socket)

  case class DBFileDelete(filePath: String)

  case class FilePathSocketArray(filePath:String)

}

class Collectors extends Actor {

  private var requertFileMap: Map[String, ArrayBuffer[Socket]] = Map.empty

  override def receive: Receive = {
    case DBFileInsert(filePath, value) =>
      if(requertFileMap.contains(filePath)){
        val socketArrayBuffer = requertFileMap(filePath)
        socketArrayBuffer += value
        sender() ! "Ok"
      }else{
        val socketArrayBuffer = new ArrayBuffer[Socket]()
        socketArrayBuffer += value
        requertFileMap += (filePath -> socketArrayBuffer)
        sender() ! "Ok"
      }

    case DBFileDelete(filePath) =>
      requertFileMap -= (filePath)
      sender() ! "Ok"

    case FilePathSocketArray(filePath) =>
      val socketArrayBuffer = requertFileMap(filePath)
      requertFileMap -= (filePath)
      sender() ! socketArrayBuffer
  }
}


