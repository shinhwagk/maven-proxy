package org.gk.db

import org.gk.workers.down.Work

/**
 * Created by goku on 2015/7/30.
 */

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