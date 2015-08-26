package org.gk.download

import akka.actor._
import com.typesafe.config.ConfigFactory
import org.gk.download.DownManager.{DownFailure, DownSuccess}
import org.gk.download.DownMaster.Download

/**
 * Created by goku on 2015/7/22.
 */

object DownManager {

  case class DownSuccess(fileUrl: String, fileArrayByte: Array[Byte])

  case class DownFailure(fileUrl: String, code: Int)

  def main(args: Array[String]) {
    val system = ActorSystem("DownloasServer")
    //    val url = scala.io.StdIn.readLine()
    //    val fileOS = scala.io.StdIn.readLine()
    system.actorOf(Props[Down]) !("https://codeload.github.com/apache/incubator-zeppelin/zip/master", Some("/tmp/xxx.zip"))

  }


  class Down extends Actor {
    override def receive: Actor.Receive = {
      case (url: String, fileOS: Option[String]) => {
        context.actorOf(Props(new DownManager(self))) !(url, fileOS)

      }
      case DownSuccess(fileUrl, fileArrayByte) => {
        println("haha")
      }
    }
  }

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