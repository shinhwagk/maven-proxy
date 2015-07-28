import java.net.{HttpURLConnection, URL}

import akka.actor._
import akka.actor.Actor.Receive
import akka.routing.RoundRobinPool
import org.gk.workers.HeadParser

/**
 * Created by gk on 15/7/27.
 */
object test7 {
  def main(args: Array[String]) {
    val system = ActorSystem("MavenProxy")
    val ab = system.actorOf(Props[ab], name = "a")

    ab ! "a"
    ab ! "b"
    ab ! "c"
   }
  class bb extends Actor {
    var bbb:Int = _
    override def receive: Actor.Receive = {
      case (ccc:Int,l:String) => {
        bbb +=ccc
        sender() ! (bbb,l)
      }
    }
  }
  class ab extends Actor {
    val bb = context.actorOf(RoundRobinPool(3).props(Props[bb]),name ="downWorker")
//    val bb = context.actorOf(Props(new bb),name ="downWorker")
    override def receive: Receive = {
      case a:String =>{
          bb ! (1,a)
      }
      case (ccc:Int,l:String)=>{
        println(ccc+l)
      }
    }
  }
}

