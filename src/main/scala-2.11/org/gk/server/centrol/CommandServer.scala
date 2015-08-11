package org.gk.server.centrol

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.gk.server.centrol.CommandServer._

/**
 * Created by goku on 2015/8/11.
 */
object CommandServer {

  case class addRepository(name: String, url: String, priority: Int, start: Boolean)

  case class deleteRepository(name: String)

  case object listRepository

}

class CommandServer extends Actor {
  override def receive: Receive = {
    case addRepository(repoName, repoUrl, priority, start) => {
      import org.gk.server.db._
      DML.addRepository(repoName, repoUrl, priority, start)
    }
    case deleteRepository(repoName) => {
      import org.gk.server.db._
      DML.deleteRepository(repoName)
    }
    case listRepository => {
      import org.gk.server.db._
      DML.listRepoitory
    }
  }
}
