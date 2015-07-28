import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import org.gk.workers.HeadParser

/**
 * Created by gk on 15/7/27.
 */
object test7 {
  def main(args: Array[String]) {
    val system = ActorSystem("MavenProxy")
    val a = system.actorOf(Props(new a("b")), name = "a")
    a ! "a"
    a ! "b"
   }

  class a(abc:String) extends Actor {
    val bz = context.actorOf(Props[b], name = "bz")
    var aaa: String = _
    println(aaa)

    override def receive: Receive = {
      case a: String => aaa = a; println(aaa)

    }
  }
  class b extends Actor{
    val c = context.actorOf(Props[c], name = "b")
    override def receive: Receive = {
      case "b" => c ! "c" ;
      case "c" => sender() ! "z";
      case "z" => println("aaa")
    }
  }
  class c extends Actor{
    override def receive: Receive = {
      case "c" => sender() ! "c"
      case "z" => println("haha")
    }
  }
}

