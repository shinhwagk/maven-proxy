package org.gk.download

import java.net.Socket

import akka.actor.{Actor, ActorRef, Props}
import org.gk.download.DownManager.{DownFailure, DownSuccess}
import org.gk.download.DownMaster.Download
import org.gk.repository.RepoManager.{DownFileFailure, DownFileSuccess}

/**
 * Created by goku on 2015/7/22.
 */

object DownManager {

  case class DownSuccess(fileUrl: String, fileArrayByte: Array[Byte])

  case class DownFailure(fileUrl: String, code: Int)

}

class DownManager(requestActorRef: ActorRef) extends Actor with akka.actor.ActorLogging {

  override def receive: Actor.Receive = {
    //    case (filePath: String, fileUrl: String, fileOS: Option[String]) =>
    //      context.watch(context.actorOf(Props(new DownMaster(self, Some(filePath), fileUrl, fileOS)))) ! Download

    case (socket: Socket, url: String) =>
      context.watch(context.actorOf(Props(new DownMaster(self, url, None)))) ! Download
    case (url: String) =>
      println(url + "a")
      context.watch(context.actorOf(Props(new DownMaster(self, url, None)))) ! Download
    case DownSuccess(fileUrl, fileArrayByte) =>
      requestActorRef ! DownSuccess(fileUrl, fileArrayByte)

    case DownFailure(fileUrl, code) =>
      requestActorRef ! DownFailure(fileUrl, code)
  }
}