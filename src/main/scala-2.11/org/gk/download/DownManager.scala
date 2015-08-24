package org.gk.download

import akka.actor.{Actor, Props}
import org.gk.download.DownManager.DownSuccess
import org.gk.download.DownMaster.Download
import org.gk.maven.RepoManager.DownFileSuccess
import org.gk.server.workers.ActorRefWorkerGroups

/**
 * Created by goku on 2015/7/22.
 */

object DownManager {

  case class DownSuccess(filePath: String, fileArrayByte: Array[Byte])

}
class DownManager extends Actor with akka.actor.ActorLogging {

  override def receive: Actor.Receive = {
    case (filePath: String, fileUrl: String, fileOS: Option[String]) =>
      context.watch(context.actorOf(Props(new DownMaster(self, filePath, fileUrl, fileOS)))) ! Download

    case DownSuccess(filePath, fileArrayByte) => {
      ActorRefWorkerGroups.repoManager ! DownFileSuccess(filePath, fileArrayByte)

    }
  }
}