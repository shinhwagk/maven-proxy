package org.gk.server.workers

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import akka.actor.Actor
import org.gk.server.workers.Doorman.StoreRequert
import org.gk.server.workers.RepoManager.RequertFile

/**
 * Created by goku on 2015/7/27.
 */

object HeadParser{


  case class RequertFilePath(socket: Socket)

}


class HeadParser extends Actor with akka.actor.ActorLogging {
import HeadParser._
  override def receive: Receive = {
    case RequertFilePath(socket: Socket) => {
      println("xxx22")
      sender() ! StoreRequert(getFilePath(socket),socket)
      println("xxxx")
    }
  }


  def getHeadInfo(socket: Socket): Map[String, String] = {
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    var a: Map[String, String] = Map.empty
    var templine = br.readLine()
    println(templine+"    head")
//    val b = templine.split(" ")
//    a += ("PATH" -> b(1))

    templine = br.readLine()

    while (templine != null && templine != "") {
      println(templine)
      val b = templine.split(":")

      b(0) match {
        case "Connection" => a += (b(0) -> b(1))
        case _ => None
      }
      templine = br.readLine()
    }
    a
  }

  def getFilePath(socket: Socket): String = {
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val templine = br.readLine()
    val b = templine.split(" ")
    println(b(1))
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    b(1)
  }
}