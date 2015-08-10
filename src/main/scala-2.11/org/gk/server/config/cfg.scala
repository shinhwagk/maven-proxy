package org.gk.server.config

import com.typesafe.config.ConfigFactory

import scala.collection.mutable.Map

/**
 * Created by goku on 2015/7/22.
 */
object cfg {

  case class RepoInfo(url:String,port:Int)
  val config = ConfigFactory.load()

  def getRemoteRepoMap ={

    var RepositoryMap:Map[String,RepoInfo] = Map.empty
    val a = config.getList("RemoteRepositoryList").unwrapped()
    import scala.collection.JavaConversions._
    for (c <- a){
      val RepositoryInfo1 = mapAsScalaMap(c.asInstanceOf[java.util.Map[String,String]])
      val repoName = RepositoryInfo1("name")
      val repoUrl = RepositoryInfo1("url")
      val RepositoryInfo2 = mapAsScalaMap(c.asInstanceOf[java.util.Map[String,Int]])
      val repoPort = RepositoryInfo2("port")
      RepositoryMap += (repoName -> RepoInfo(repoUrl, repoPort) )
    }
    RepositoryMap
  }

  def getPerProcessForBytes: Int = {
    config.getInt("PerProcessForBytes")
  }

  def getMavenPorxyManagePort: Int = {
    config.getInt("MavenPorxyManagePort")
  }


  def getLocalMainDir: String = {
    config.getString("LocalMainDir")
  }

  def getLocalRepoDir: String = {
    getLocalMainDir + "/cache"
  }


  def getMavenProxyPost:Int = {
    config.getInt("MavenPorxyPort")
  }
}




