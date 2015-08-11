package org.gk.client.centrol

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

/**
 * Created by goku on 2015/8/11.
 */
object client {
  val system = ActorSystem("MavenProxyClient", ConfigFactory.load("client"))
  val commandClient = system.actorOf(Props[CommandClient])

  def main(args: Array[String]) {

    val parameter = scala.io.StdIn.readLine("请输入")

    parameter match {
      case "--add" =>
        commandClient ! "add"
      case _ if parameter.startsWith("--add") && parameter.split(" ").length == 5 =>
        commandClient ! "add"
      case _ if parameter.startsWith("--del") =>
        commandClient ! "del"
      case _ if parameter.startsWith("--list") =>
        commandClient ! "list"
    }
  }
}
