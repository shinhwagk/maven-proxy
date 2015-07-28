import java.net.{HttpURLConnection, URL}

import akka.actor.{ReceiveTimeout, Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import akka.routing.RoundRobinPool
import org.gk.workers.HeadParser

/**
 * Created by gk on 15/7/27.
 */
object test7 {
  def main(args: Array[String]) {
    val system = ActorSystem("MavenProxy")
    val a = system.actorOf(Props[a], name = "a")
    val downWorker = system.actorOf(RoundRobinPool(5).props(Props[a]),name ="downWorker")
    a ! "a"
    a ! "b"
   }

  class a extends Actor {

    import akka.actor.OneForOneStrategy
    import akka.actor.SupervisorStrategy._
    import scala.concurrent.duration._
    override def receive: Receive = {
      case a: String => {
        println(a)
        val url = new URL("https://repo.maven.apache.org/maven2/HTTPClient/HTTPClient/maven-metadata.xml")
        val conn = url.openConnection().asInstanceOf[HttpURLConnection];
        try{
          conn.setConnectTimeout(100)
          println(conn.getResponseCode)
        }catch {
          case ex:Exception => {
            println(ex.getMessage);
          }
        }
        println("xxxxxxxxxxxxxxxxxxxxxxx")

      }
//      case ReceiveTimeout =>{
//        // No progress within 15 seconds, ServiceUnavailable
//        println("xxxxxxxx")
//      }
    }
//    context.setReceiveTimeout(3 seconds)
  }
}

