package org.gk.config

import com.typesafe.config.ConfigFactory

import scala.collection.mutable.Map

/**
 * Created by goku on 2015/7/22.
 */
object cfg {

  val config = ConfigFactory.load()

  def getRepositoryMap ={

    val RepositoryMap:Map[String,String] = Map.empty
    val a = config.getList("RemoteRepositoryList").unwrapped()
    import scala.collection.JavaConversions._
    for (c <- a){
      val Repository = mapAsScalaMap(c.asInstanceOf[java.util.Map[String,String]])
      RepositoryMap += (Repository("name") -> Repository("url") )
    }
    RepositoryMap
  }

  def getLocalRepositoryDir: String ={
      config.getString("LocalRepositoryDir")
  }

  def getMPPort:Int={
    config.getInt("port")
  }

  def main(args: Array[String]) {
//    println(getLocalRepositoryDir)
  val a = getRepositoryMap

    for((k,v)<-getRepositoryMap){
      println(k)
    }
//    repositoryMap.filter(repo => repo._2)
  }

}




