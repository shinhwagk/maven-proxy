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

    a ! "a"
    a ! "b"

    b ! "a"
    b ! "b"

    for( i <- 1 to 100){
      val a = system.actorOf(Props[A])
      a ! "a"


      a ! "b"
    }

  }
}

class A extends Actor {
  var a:Int = _
  override def receive: Receive = {
    case "a" =>
      a = 1
      println(self.path.name + " " + a)
    case "b" =>
      println(a)
  }

}