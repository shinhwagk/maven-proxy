package org.gk.server

import java.net.ServerSocket

import org.gk.server.config.cfg
import org.gk.server.tool.RequestHeader
import org.gk.server.workers.ActorRefWorkerGroups
import org.gk.server.workers.Anteroom.JoinAnteroom

/**
 * Created by goku on 2015/8/19.
 */
object HttpProxy {
  val ss = new ServerSocket(cfg.getMavenProxyServicePost + 1);
  def main(args: Array[String]) {
    while(true)
      ActorRefWorkerGroups.anteroom ! JoinAnteroom(RequestHeader(ss.accept()))
  }
}
