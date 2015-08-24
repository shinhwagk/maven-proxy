package org.gk.proxy

import java.net.Socket

import akka.actor.{Props, Actor}
import org.gk.download.DownManager
import org.gk.download.DownManager.DownSuccess
import org.gk.repository.Returner

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/8/24.
 */
class ProxyMain extends Actor {
  var anteroom: Map[String, ArrayBuffer[Socket]] = Map.empty
  val downManager = context.actorOf(Props(new DownManager(self)), name = "DownManager")

  override def receive: Receive = {
    case (socket: Socket, requestLine: String) =>
      requestLine.split(" ")(0) match {
        case "GET" =>
          println("GET请求....")
          if (anteroom.contains(requestLine.split(" ")(1))) {
            anteroom(requestLine.split(" ")(1)) += socket

          } else {
            anteroom += (requestLine.split(" ")(1) -> ArrayBuffer(socket))
            downManager ! requestLine.split(" ")(1)
          }

        case "CONNECT" =>
      }

    case DownSuccess(fileUrl: String, fileArrayByte: Array[Byte]) =>
      anteroom(fileUrl).foreach(p => {
        context.actorOf(Props(new Returner)) !(p, fileArrayByte)
      })
      anteroom -= (fileUrl)

  }
}
