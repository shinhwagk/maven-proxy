package test;
import akka.actor.{ Actor, Props, ActorSystem, ExtendedActorSystem }
import com.typesafe.config.ConfigFactory

object MyApp extends App {
  val actorSystem2 = ActorSystem("actorSystem2");

  val a = actorSystem2.actorOf(Props(new Actor {
    def receive = {
      case x: String =>
        Thread.sleep(1000)
        println("RECEIVED MESSAGE: " + x)
    }
  }), "simplisticActor")


  a ! "TEST 1"
  a ! "TEST 2"
  a ! "TEST 3"

  Thread.sleep(5000)

}