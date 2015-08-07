import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive

/**
 * Created by goku on 2015/8/5.
 */


object bbbb {
  def main(args: Array[String]) {
    val system = ActorSystem("TEST")

    //preStart在actorOf的时候执行
    val a = system.actorOf(Props[A], name = "a")


    //在stop的时候执行postStop
    val b = system.actorOf(Props[A], name = "ab")

    val ccc =abc(1)
    a ! ccc


  }
}

class A extends Actor {
  var a:Int = _
  override def receive: Receive = {
    case abc(a)=>
    println(a)
    case ccc:abc =>
      println(ccc.a)
  }
}

case class abc(a:Int)