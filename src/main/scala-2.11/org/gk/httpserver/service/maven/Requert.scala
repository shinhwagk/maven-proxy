package org.gk.httpserver.service.maven

import java.io.{BufferedReader, File, InputStream, InputStreamReader}
import java.net.Socket

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.gk.httpserver.ResponseSocket
import org.gk.log.GkConsoleLogger

/**
 * Created by goku on 2015/7/23.
 */
class Requert extends Actor {
  def parseHttpHead(is:InputStream) = {
    val isr = new InputStreamReader(is)
    val br = new BufferedReader(isr)

    import scala.collection.mutable.Map
    val headMap = Map[String,String]()
    var line = br.readLine()
    while(line != null && !line.isEmpty){
      line match {
        case _ if line.contains("GET") => headMap += ("path"-> line.split(" ")(1));
        case _ if line.contains("Host") => print(line) //未处理
        case _ if line.contains("Connection") => println("a")
        case _ if line.contains("Accept") => println("a")
        case _ if line.contains("User-Agent") => println("a")
        case _ if line.contains("Cache-Control") => println("a")
        case _ if line.contains("HTTPS") => println("a")
        case _ => println("b");println(line)
      }
      line = br.readLine()
    }
    headMap
  }
  def DecideLocalFileExists(filePath:String): Boolean ={
    new File(org.gk.config.cfg.getLocalRepositoryDir + filePath).exists()
  }

  //获得file的路径
//  val filePath = parseHttpHead(socket.getInputStream)("path")
  //查看自己仓库是否存在文件
//  var localFileExists = DecideLocalFileExists(filePath)

  def receive ()= {
    case socket:Socket => {
      GkConsoleLogger.info("requert处理完毕...")
      GkConsoleLogger.info("转给Resonse处理...")
//      sender() ! ResponseSocket(socket)
//      sender() !"over"
    }
  }
  println("aaaaaaaaaaaaaaaaaaa")
}
