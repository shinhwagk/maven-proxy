package org.gk.config

import com.typesafe.config.ConfigFactory

import scala.collection.mutable.Map

/**
 * Created by goku on 2015/7/22.
 */
object cfg {

  val config = ConfigFactory.load()

  def getRemoteRepoMap ={

    var RepositoryMap:Map[Int,(String,String)] = Map.empty
    val a = config.getList("RemoteRepositoryList").unwrapped()
    import scala.collection.JavaConversions._
    var RepoId = 0
    for (c <- a){
      val Repository = mapAsScalaMap(c.asInstanceOf[java.util.Map[String,String]])
      RepoId += 1
      RepositoryMap += (RepoId -> (Repository("name"), Repository("url")) )
    }
    RepositoryMap
  }

  def getRemoteRepoCentral: String = {
    config.getString("RemoteRepsCentral")
  }

  def getPerProcessForBytes: Int = {
    config.getInt("PerProcessForBytes")
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




