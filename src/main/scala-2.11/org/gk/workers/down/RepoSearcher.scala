package org.gk.workers.down

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import org.gk.config.cfg
import org.gk.workers.down.DownManager.RequertDownFile
import org.gk.workers.down.DownMaster.DownFile
import org.gk.workers.down.RepoSearcher.SearchPepo

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/28.
 */
object RepoSearcher{

  case class SearchPepo(file:String)
}
class RepoSearcher extends Actor with ActorLogging{
  override def receive: Receive = {
    case SearchPepo(file) =>
      log.info("仓库搜索{}",file)
      val fileUrl = getFileUrl(file)
      sender() ! RequertDownFile(fileUrl,file)
      log.info("找到仓库已经返回",file)

  }
  def getFileUrl(file:String): String ={
    val remoteRepMap = cfg.getRemoteRepoMap
    val getRemoteRepo_Central = cfg.getRemoteRepoCentral
    val testCentralFileUrl = getRemoteRepo_Central + file
    val fileUrl = if(getTestFileUrlCode(testCentralFileUrl) == 200 ){
      testCentralFileUrl
    }else{
      val a = remoteRepMap.filter(repo => (getTestFileUrlCode(repo._2+file) == 200))
      val b = a.map(x => x._2)
      val c = b.asInstanceOf[ArrayBuffer[String]]
      c(0)+file
    }
    fileUrl
  }
  def getTestFileUrlCode(fileUrl:String): Int ={
    import java.net.{HttpURLConnection, URL};
    val downUrl = new URL(fileUrl)
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000)
    downConn.getResponseCode
  }
}
