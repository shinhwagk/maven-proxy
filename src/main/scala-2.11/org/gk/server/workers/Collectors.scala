package org.gk.server.workers

import java.net.Socket

import akka.actor.Actor
import org.gk.server.workers.Collectors.{DBFileCreate, DBFileDelete, DBFileInsert, FilePathSocketArray}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/8/17.
 */

object Collectors {

  case class DBFileInsert(fileOS: String, value: Socket)

  case class DBFileCreate(fileOS: String, value: Socket)

  case class DBFileDelete(fileOS: String)

  case class FilePathSocketArray(filePath:String)

}

class Collectors extends Actor {

  private var requertFileMap: Map[String, ArrayBuffer[Socket]] = Map.empty

  override def receive: Receive = {
    case DBFileInsert(fileOS, value) =>
      if(requertFileMap.contains(fileOS)){
        val socketArrayBuffer = requertFileMap(fileOS)
        socketArrayBuffer += value
        sender() ! "Ok"
      }else{
        val socketArrayBuffer = new ArrayBuffer[Socket]()
        socketArrayBuffer += value
        requertFileMap += (fileOS -> socketArrayBuffer)
        sender() ! "Ok"
      }

    case DBFileDelete(fileOS) =>
      requertFileMap -= (fileOS)
      sender() ! "Ok"

    case FilePathSocketArray(fileOS) =>
      val socketArrayBuffer = requertFileMap(fileOS)
      requertFileMap -= (fileOS)
      sender() ! socketArrayBuffer
  }
}


