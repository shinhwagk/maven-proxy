package org.gk.httpserver.service

import java.net.Socket

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.gk.httpserver.service.workers.{Response, Requert}


/**
 * Created by goku on 2015/7/23.
 */
class MavenProxy(val socket:Socket) extends Actor {
  override def receive: Actor.Receive = ???
}
