package org.gk.client.centrol

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory

/**
 * Created by goku on 2015/8/11.
 */
object MavenProxyClient {
  val system = ActorSystem("MavenProxyClient", ConfigFactory.load("client"))
  val commandClient = system.actorOf(Props[CommandClient])

  def main(args: Array[String]) {

    val parameter = scala.io.StdIn.readLine("请输入")
//    val parameter = "--help"

    parameter match {
      case "--help" =>
        println("usage: maven-proty [options] [<goal(s)>]")
        println("--add, --add <arg> \t\t aaa")
        println("--del, --del <arg> \t\t bbb")
        println("--list \t\t\t\t\t bbb")
        System.exit(0)
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
