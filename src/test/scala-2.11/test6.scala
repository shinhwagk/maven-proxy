import akka.actor.{Props, ActorSystem, Actor}
import akka.actor.Actor.Receive
import com.typesafe.config.ConfigFactory
import org.gk.workers.HeadParser

/**
 * Created by gk on 15/7/26.
 */
object test6 {
  def main(args: Array[String]) {
    val system = ActorSystem("MavenProxy111")
    //  val listener = system.actorOf(RoundRobinPool(1).props(Props(new Listener)), name = "listener")
    val headParser = system.actorOf(Props[aa],"adfadfs")
    headParser ! "a"
  }

}

class aa extends Actor with akka.actor.ActorLogging{
  val a = 1
  override def receive: Receive = {
    case "a" => log.info("aaaa{}",a)
  }
}
