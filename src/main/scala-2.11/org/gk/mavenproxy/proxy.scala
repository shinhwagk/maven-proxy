package org.gk.mavenproxy

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor, ActorSystem}
import akka.routing.RoundRobinPool

/**
 * Created by goku on 2015/7/23.
 */
object proxy {
  def start(): Unit ={


    val system = ActorSystem("PiSystem")

    val listener = system.actorOf(RoundRobinPool(2).props(Props(new Listener)), name = "listener")
    listener ! "abc"
    listener ! "abc"
    listener ! "abc"
    listener ! "abc"
    listener ! "abc"
    listener ! "abc"
    listener ! "abc"
    listener ! "abc"
  }



  def main(args: Array[String]) {
    proxy.start()
  }
}
class test extends Actor{
  override def receive: Receive ={
    case "abc" =>{
      println("hahahahahah")
      println(context.self.path.name+"aaaaaaaaaaaaaa")

    }
  }
}
class Listener extends Actor {
  val test = context.actorOf(Props(new test),name = "heihei")
  def receive = {
    case "abc" => {

//      println(test.toString())
      test ! "abc"
      test ! "abc"
      println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s".format("a", "a"))
      Thread.sleep(3000)
      println(context.system.name)
    }
    case "ccc" => {
      println("ccc")
      Thread.sleep(1000)
      println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s".format("a", "a"))
    }
  }


}