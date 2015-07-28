import java.net.{HttpURLConnection, URL}
import java.util.Random

import akka.actor.SupervisorStrategy.{Resume, Restart, Stop}
import akka.actor._
import akka.event.LoggingReceive
import scala.concurrent.duration._

/**
 * Created by gk on 15/7/27.
 */
object test7 {
  def main(args: Array[String]) {
    val system = ActorSystem("MavenProxy")
    val ab = system.actorOf(Props[ab], name = "a1")

    ab ! "a"

   }

  class StorageException(msg: String) extends RuntimeException(msg)
  class ab extends Actor {

    override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 seconds) {
      case _: StorageException =>{
        println("1xxxxxxxxxxxxxxxxx")
//        Resume
//        Stop
        Restart
      }
      case _:java.lang.NullPointerException =>{
        println("zzz")
        Restart
      }
    }

    val cc = context.actorOf(Props[cc], name = "b1")
    val bb = context.watch(cc)
    override def receive: Receive = {
      case "a" =>{
        bb ! "b"
      }
      case "hahaha" =>{
        println("bbbbbbbb")
      }
      case _ =>
        println("vvvvvvvvv")

    }
  }
  class cc extends Actor{
    override def receive: Receive = {
      case a:String =>{
        println("mmm")
        abc
        println("xxxx")
      }
    }
  }
  def abc(): Unit ={
    val m = new Random()
    val m1 = m.nextInt(100)
    println(m1)
    if( m1%2 == 0)
    {
      throw new StorageException("hahaha")
    }
  }
}

