package org.gk.workers

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import akka.actor.{Props, Actor}

/**
 * Created by goku on 2015/7/27.
 */
class HeadParser extends Actor {
  val terminator = context.actorOf(Props[Terminator])
  val repoManager = context.actorOf(Props[RepoManager])

  override def receive: Receive = {
    case socket:Socket =>{
      val file  = getFile(socket)
      repoManager ! (file,socket)
//      headParse(socket)
    }
  }

//  def headParse(socket: Socket): Unit ={
//    val headBuffers = new BufferedReader(new InputStreamReader(socket.getInputStream))
//    val headFirstLine = headBuffers.readLine()
//    headFirstLine match {
//      case null => terminator ! (204,socket)
//      case _ if headFirstLine.split(" ").length !=3 => terminator ! (204,socket)
//      case _ if headFirstLine.split(" ")(1) == "/" => terminator ! (204,socket)
//      case _ => repoManager ! (headFirstLine.split(" ")(1),socket)
//    }
//  }

//  def deBugLockHead(socket:Socket): Unit ={
//    val headBuffers = new BufferedReader(new InputStreamReader(socket.getInputStream))
//    for(i <- 1 to  10){
//      print(i+" : ")
//      println(headBuffers.readLine())
//    }
//  }

  def getFile(socket:Socket): String ={
    val headBuffers = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val headFirstLine = headBuffers.readLine()
    headFirstLine.split(" ")(1)
  }
}