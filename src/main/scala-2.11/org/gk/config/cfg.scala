package org.gk.config

import com.typesafe.config.ConfigFactory

import scala.collection.mutable.Map

/**
 * Created by goku on 2015/7/22.
 */
object cfg {

  val config = ConfigFactory.load()

  def getRemoteRepoMap ={

    val RepositoryMap:Map[String,String] = Map.empty
    val a = config.getList("RemoteRepositoryList").unwrapped()
    import scala.collection.JavaConversions._
    for (c <- a){
      val Repository = mapAsScalaMap(c.asInstanceOf[java.util.Map[String,String]])
      RepositoryMap += (Repository("name") -> Repository("url") )
    }
    RepositoryMap
  }

  def getDownFilePorcessNumber:Int = {
    config.getString("DownFilePorcessNumber").toInt
  }
  def getRemoteRepoCentral: String = {
    config.getString("RemoteRepsCentral")
  }

  def getLocalRepoDir: String ={
      config.getString("LocalRepoDir")
  }

  def getLocalRepoTmpDir: String ={
    config.getString("LocalRepoTmpDir")
  }

  def getMavenProxyPost:Int={
    config.getInt("MavenPorxyPort")
  }

  def main(args: Array[String]) {
//    println(getLocalRepositoryDir)
  val a = getRemoteRepoMap

    for((k,v)<-getRemoteRepoMap){
      println(k)
    }
//    repositoryMap.filter(repo => repo._2)
  }

}




