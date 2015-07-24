import java.net.{HttpURLConnection, URL}

import akka.actor._
import akka.routing.RoundRobinPool

/**
 * Created by goku on 2015/7/24.
 */
object test4 {
  val system = ActorSystem("MavenProxy")
  val listener = system.actorOf(Props[testa], name = "listener")
  var lastSender = system.deadLetters

  def main(args: Array[String]) {
    listener !"abb"
    listener !"abb"
    listener !"abb"
    listener !"abb"
    listener !"abb"
//    system.shutdown()
  }

  class testa extends Actor {
    val child = context.actorOf(RoundRobinPool(1).props(Props[WatchActor]), "child")
    override def receive: Receive = {
      case "abb" => {
       println("xxxxxxxxx")
        val a = new URL("http://aaaaa.com")
        val v = a.openConnection().asInstanceOf[HttpURLConnection];
        v.setReadTimeout(1000000)
      }

    }
  }
}




class WatchActor extends Actor {


  def receive = {
    case "abc" =>println("haha")

  }
}