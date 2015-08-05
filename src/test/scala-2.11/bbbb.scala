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

    Thread.sleep(2000)
    //在stop的时候执行postStop
    system.stop(a)

    Thread.sleep(2000)
    val b = system.actorOf(Props[A], name = "a")
    b ! "b"
  }
}


class A extends Actor {
  override def receive: Receive = {
    case "a" =>
      println("a")
    case "b" =>
      throw new Exception("hwaaa ")
  }
  override def preStart {
    println("actor:" + self.path + ",child preStart .")
  }
  override def postStop {
    println("actor:" + self.path + ",child postStop .")
  }
  override def preRestart(reason: Throwable, message: Option[Any]) {
    println("actor:" + self.path + ",preRestart child, reason:" + reason + ", message:" + message)
  }
  override def postRestart(reason: Throwable) {
    println("actor:" + self.path + ",postRestart child, reason:" + reason)
    Thread.sleep(3000)
    context.stop(self)
    println("jieshu")
    Thread.sleep(3000)
    println("jieshu2")
    Thread.sleep(3000)
  }
}