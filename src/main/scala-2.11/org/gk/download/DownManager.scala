package org.gk.download

import akka.actor.{Actor, ActorRef, Props}
import org.gk.download.DownManager.{DownFailure, DownSuccess}
import org.gk.download.DownMaster.Download

/**
 * Created by goku on 2015/7/22.
 */

object DownManager {

  case class DownSuccess(fileUrl: String, fileArrayByte: Array[Byte])

  case class DownFailure(fileUrl: String, code: Int)

}

class DownManager(requestActorRef: ActorRef) extends Actor with akka.actor.ActorLogging {

  override def receive: Actor.Receive = {
    case (fileUrl: String, fileOS: Option[String]) =>
      println(fileUrl)
      context.watch(context.actorOf(Props(new DownMaster(self, fileUrl, fileOS)))) ! Download

    case (url: String) =>
      context.watch(context.actorOf(Props(new DownMaster(self, url, None)))) ! Download

    case DownSuccess(fileUrl, fileArrayByte) =>
      val downMasterActorRef = sender()
      context.unwatch(downMasterActorRef)
      context.stop(downMasterActorRef)
      requestActorRef ! DownSuccess(fileUrl, fileArrayByte)

    case DownFailure(fileUrl, code) =>
      val downMasterActorRef = sender()
      context.unwatch(downMasterActorRef)
      context.stop(downMasterActorRef)
      requestActorRef ! DownFailure(fileUrl, code)
  }
}