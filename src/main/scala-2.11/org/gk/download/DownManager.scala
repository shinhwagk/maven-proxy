package org.gk.download

import akka.actor.{Actor, Props}
import org.gk.download.DownMaster.Download

/**
 * Created by goku on 2015/7/22.
 */

class DownManager extends Actor with akka.actor.ActorLogging {

  override def receive: Actor.Receive = {
    case (fileUrl: String, fileOS: String) =>
      context.watch(context.actorOf(Props(new DownMaster(fileUrl, fileOS)))) ! Download
  }

}