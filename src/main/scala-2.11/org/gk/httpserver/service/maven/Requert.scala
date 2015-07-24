package org.gk.httpserver.service.maven

import java.io.{BufferedReader, File, InputStream, InputStreamReader}
import java.net.Socket

import akka.actor.{Props, Actor}
import org.gk.httpserver.CaseResponse

import org.gk.log.GkConsoleLogger

/**
 * Created by goku on 2015/7/23.
 */
object Requert{
  def props(socket:Socket):Props = Props(new Requert(socket))
}

class Requert(socket:Socket) extends Actor {

  def receive ()= {
    case "requert" => {
      GkConsoleLogger.info("requert处理者,接受到请求，准备处理...")
      GkConsoleLogger.info("requert处理者: 获取请求头信息...")
      val filepath = parseHttpHead(socket.getInputStream)
      if(filepath !="no") {
        GkConsoleLogger.info("requert处理者: 头信息获取完毕...")
        GkConsoleLogger.info("requert处理者: 转移requert给response...")
        sender() ! CaseResponse(filepath, socket)
      }else{
        socket.close()
      }
    }
  }


  def parseHttpHead(is:InputStream) = {
    val isr = new InputStreamReader(is)
    val br = new BufferedReader(isr)

    var path:String = ""
    var line = br.readLine()
    while(line != null && !line.isEmpty){
      line match {
        case _ if line.contains("GET") => if(line.split(" ").length >=3){
          println(line.split(" ").length);
          path = line.split(" ")(1)
        } else{
          path ="no"
        }
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
    path
  }



}
