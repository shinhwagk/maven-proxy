import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import org.gk.workers.down.Work

/**
 * Created by goku on 2015/7/30.
 */
object HotSwapActor{
  def main(args: Array[String]) {
    val a = WorkDownFileDB
    a.saveDownMap("aaaa")
    a.load("aaaa").saveDownMap("a","b")
    a.saveDownMap("bbbb")
    a.load("bbbb").saveDownMap("a1","b1")
    a.load("bbbb").saveDownMap("a2","b2")

    for((k,v) <- WorkDownFileDB.downFileMap){
      println(k+"xxxx")
      for((k,v) <- v.actorIdMap){
        println(k + " " +v)
      }
    }

  }
}

class WorkerActorDownDB{
  var actorIdMap = Map[String, Work]()
  var actorId = 0;
  def addActor(actorName:String, work:Work) = synchronized {
    actorIdMap += (actorName -> Work)
  }
  def load(key: String): String = synchronized {
    actorIdMap(key)
  }
  def getNewActorId:Int  = synchronized {
    actorId += 1
    actorId
  }
}

object WorkDownFileDB{
  var downFileMap = Map[String,WorkerActorDownDB]()

  def addDownFile(fileName:String):Unit = synchronized {
    downFileMap += (fileName -> new WorkerActorDownDB)

  }
  def load(key: String): WorkerActorDownDB = synchronized {
    downFileMap(key)
  }

  def delDownFile(key:String): Unit = synchronized{
    downFileMap -= key
  }
}