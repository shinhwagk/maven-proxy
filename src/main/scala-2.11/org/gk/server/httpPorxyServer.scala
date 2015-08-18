package org.gk.server

import java.io.BufferedInputStream
import java.net.ServerSocket

import org.gk.server.config.cfg
import org.gk.server.workers.down.DownMaster.Download
import org.gk.server.workers.{Headers, ActorRefWorkerGroups}
import org.gk.server.workers.down.DownManager.RequertDownFile

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/8/18.
 */
object httpPorxyServer {
  val ss = new ServerSocket(9994);

  def main(args: Array[String]) {
    while (true) {
      val socket = ss.accept()
      val bis = new BufferedInputStream(socket.getInputStream)

      val headersBuffer = new ArrayBuffer[Int]
      var a = bis.read()
      while (a != -1) {
        headersBuffer += a
        a = bis.read()
      }
      val b = headersBuffer.toArray
      val c = b.map(_.toByte)
      println(new String(c))
      println("")
    }
  }
}
