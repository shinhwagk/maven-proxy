package org.gk

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
 * Created by gk on 15/8/22.
 */
object MavenProxyMain extends App {
  val system = ActorSystem("MavenProxyServer", ConfigFactory.load("server"))
  val mavenRepository = system.actorOf(Props[MavenRepository], name = "MavenRepository")
  val mavenHttpproxy = system.actorOf(Props[MavenHttpProxy], name = "MavenHttpProxy")
  val headerParser = system.actorOf(Props[HeaderParser], name = "HeaderParser")

  import org.gk.server.db._

  InitDatabase.initMavenProxy
  println("系统已经启动...")


  mavenRepository ! "start"
  mavenHttpproxy ! "start"
}
