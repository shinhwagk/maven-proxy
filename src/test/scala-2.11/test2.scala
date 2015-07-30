import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive

/**
 * Created by goku on 2015/7/30.
 */
object HotSwapActor{
  def main(args: Array[String]) {
    var a = Map[String,Option[Int]]()
    var backlog = IndexedSeq.empty[(String, DownFileActorRefDB)]
    backlog :+= ("abc",DownFileActorRefDB("AAA"))
    backlog :+= ("abc",DownFileActorRefDB("AAA"))
    println(backlog.)
  }
}

case class DownFileDB()
case class DownFileActorRefDB(ActorName:String)
