package org.gk.server.workers

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import org.gk.server.centrol.CommandServer
import org.gk.server.workers.down.{DownMaster, DownManager}

/**
 * Created by goku on 2015/8/12.
 */
object ActorRefWorkerGroups {
  val system = ActorSystem("MavenProxyServer", ConfigFactory.load("server"))
  val repoManager = system.actorOf(Props[RepoManager], name = "RepoManager")
  val downManager = system.actorOf(Props[DownManager], name = "DownManager")
  val terminator = system.actorOf(Props[Terminator], name = "Terminator")
  val downMaster = system.actorOf(Props[DownMaster], name = "DownMaster")
  val anteroom = system.actorOf(Props[Anteroom], name = "Anteroom")

  def startCommandServerActorRef={
    system.actorOf(Props[CommandServer], name = "CommandServer")
  }
}
