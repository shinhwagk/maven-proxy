package org.gk.server

import java.net.ServerSocket

import org.gk.server.config.cfg
import org.gk.server.workers.down.DownMaster.Download
import org.gk.server.workers.{Headers, ActorRefWorkerGroups}
import org.gk.server.workers.down.DownManager.RequertDownFile

/**
 * Created by goku on 2015/8/18.
 */
object httpPorxyServer {
  val ss = new ServerSocket(9996);


  println("系统已经启动...")

  while (true) {
    val socket = ss.accept()
    val headers = new Headers(socket)
//    ActorRefWorkerGroups.downMaster ! Download(socket, fileUrl, filePath)
  }
}
