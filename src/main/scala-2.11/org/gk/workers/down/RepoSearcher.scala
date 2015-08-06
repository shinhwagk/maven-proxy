package org.gk.workers.down

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorLogging}
import org.gk.config.cfg
import org.gk.workers.DownFileInfoBeta2
import org.gk.workers.down.DownManager.{DownLoadFile}
import org.gk.workers.down.DownMaster.DownFile
import org.gk.workers.down.RepoSearcher.SearchPepo

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/28.
 */
object RepoSearcher{

  case class SearchPepo(downFileInfoBeta2:DownFileInfoBeta2)
}
class RepoSearcher extends Actor with ActorLogging{
  override def receive: Receive = {
    case SearchPepo(downFileInfoBeta2) =>
      val file = downFileInfoBeta2.file
      log.info("仓库搜索{}",file)
      val fileURL = getFileUrl(file)
      val downFileInfoBeta3 = downFileInfoBeta2.getDownFileInfoBeta3(fileURL)
      sender() ! DownLoadFile(downFileInfoBeta3)
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
    downConn.setConnectTimeout(50000)
    downConn.getResponseCode
  }
}
