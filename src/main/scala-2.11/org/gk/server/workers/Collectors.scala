package org.gk.server.workers

import java.net.Socket

import akka.actor.Actor
import org.gk.server.workers.Collectors._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/8/17.
 */

object Collectors {

  case class JoinFileDownRequestSet(fileOS: String, value: Socket)

  case class DBFileCreate(fileOS: String, value: Socket)

  case class DBFileDelete(fileOS: String)

  case class FilePathSocketArray(filePath:String)

}

class Collectors extends Actor {

  private var requertFileMap: Map[String, ArrayBuffer[Socket]] = Map.empty

  override def receive: Receive = {
    case JoinFileDownRequestSet(fileOS, value) =>
      if(requertFileMap.contains(fileOS)){
        val socketArrayBuffer = requertFileMap(fileOS)
        socketArrayBuffer += value
      }else{
        val socketArrayBuffer = new ArrayBuffer[Socket]()
        socketArrayBuffer += value
        requertFileMap += (fileOS -> socketArrayBuffer)
        sender() ! "Ok"
      }

    case FilePathSocketArray(fileOS) =>
      val socketArrayBuffer = requertFileMap(fileOS)
      requertFileMap -= (fileOS)
      sender() ! socketArrayBuffer
  }
}


