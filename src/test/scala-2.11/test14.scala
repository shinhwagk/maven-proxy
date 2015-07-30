
import java.io.RandomAccessFile
import java.lang.RuntimeException
import java.net.{HttpURLConnection, URL}

import akka.actor.SupervisorStrategy.{Resume, Restart, Stop}
import akka.actor._
import akka.actor.Actor.Receive
import akka.event.LoggingReceive
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigFactory
import org.gk.workers.HeadParser
import scala.concurrent.duration._
import scala.util.Random
import scala.util.control.Exception
import akka.actor.{ ActorSystem, ActorRef, Props, Terminated }

/**
 * Created by gk on 15/7/26.
 */
object test14 {
  val config = ConfigFactory.parseString(

    """
    akka.loglevel = "DEBUG"
    akka.actor.debug {
      receive = on
      lifecycle = on
    }
                                         """)
  def main(args: Array[String]) {
    val system = ActorSystem("MavenProxy111",config)
    val a = system.actorOf(Props[A], "a")
    a ! "a"
//    a ! "a"
//    a ! "a"

  }
}



class A extends Actor {
  var a = 0
  import context.dispatcher
//  val escalator = OneForOneStrategy() {
//    case _: Exception =>
//      Resume
//  }
//  val b = context.watch(context.actorOf(RoundRobinPool(2,supervisorStrategy = escalator).props(routeeProps =Props[B]),"b"))


//  val escalator = OneForOneStrategy(maxNrOfRetries = 1,
//    withinTimeRange = 5 seconds) {
//    case e:Exception ⇒
//      println(e.getMessage)
//      Stop
//  }
//  val b = context.watch(context.actorOf(RoundRobinPool(2, supervisorStrategy = escalator).props(
//    routeeProps = Props[B])))
override val supervisorStrategy = OneForOneStrategy(){
  case _: Exception => Stop
}
  val b1 = context.watch(context.actorOf(Props[B],name= "b1"))
//  val b2 = context.watch(context.actorOf(Props[B],name= "b2"))

  def receive = LoggingReceive {
    case "a" =>{
      b1 ! 1
//      b2 ! 1
    }
    case Terminated(actorRef) =>{
      println(actorRef.path.name+"被关闭")
      context.watch(context.actorOf(Props[B],name= actorRef.path.name)) ! 1
    }
  }

}
class B extends Actor {

  override def preStart: Unit ={
    println(self.path.name+"构造器")
  }
  def receive = {
    case b:Int =>
      down("https://repo.maven.apache.org/maven2/org/scala-lang/scala-library/2.10.4/scala-library-2.10.4.jar","z:\\zzz.jar")
  }

  def down(url:String,fileOs:String):String = {

    println("开始下载")
    val downUrl = new URL(url);
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(2000)
    val is = downConn.getInputStream();
    val workFileLength = downConn.getContentLength;
    val raf = new RandomAccessFile(fileOs, "rwd");

    var currentLength = 0
    var start = 0
    var len = 0

    val buffer = new Array[Byte](workFileLength)
    var startTime = System.currentTimeMillis()
    while (len != -1 && workFileLength != currentLength) {
      len = is.read(buffer, start, workFileLength - currentLength)
      start += len
      currentLength += len
      println(currentLength + "/" + workFileLength)
//      println(abc(currentLength))

      Thread.sleep(1000)
    }

    raf.write(buffer)
    is.close()
    raf.close()
    println("线程:{},下载完毕")
    "WorkerDownLoadSuccess"

  }


}



