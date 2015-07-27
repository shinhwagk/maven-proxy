import java.net.{HttpURLConnection, URL}

import akka.actor._
import akka.routing.RoundRobinPool

/**
 * Created by goku on 2015/7/24.
 */
object test4 {
  val system = ActorSystem("MavenProxy")
  val listener = system.actorOf(Props(new testa), name = "listener")
//  val listener = system.actorOf(RoundRobinPool(1).props(Props(new testa)), name = "listener")

//  var lastSender = system.deadLetters

  def main(args: Array[String]) {
    listener ! "1"
    println("aaaaaaaaaaaaaaaaaaaaaa")
    listener ! "1"
    println("bbbb")
//    system.shutdown()

  }
  case class bbb(a:Int)
  class testa extends Actor {
    val child = context.actorOf(RoundRobinPool(1).props(Props[WatchActor]), "child")
    override def receive: Receive = {
      case "1" =>{
        println("xxx")
        Thread.sleep(100000)
      }
      case a:Tuple2[Int, String]  => {
        println("xxxxxxxxx" + a._2)

       println("xxxxxxxxx" + a._2)
//        val a = new URL("http://aaaaa.com.z")
//        val v = a.openConnection().asInstanceOf[HttpURLConnection];
//        v.setReadTimeout(1000)
//        println(v.getResponseCode)
//        println("sssss")
      }

    }
  }
}




class WatchActor extends Actor {


  def receive = {
    case "abc" =>println("haha")

  }
}