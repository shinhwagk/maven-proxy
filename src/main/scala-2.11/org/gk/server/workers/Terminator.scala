package org.gk.server.workers

import java.io.{PrintWriter, BufferedOutputStream}

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import java.net.Socket;

/**
 * Created by gk on 15/7/26.
 */
class Terminator extends Actor with akka.actor.ActorLogging{
  override def receive: Receive = {
    case socket:Socket =>
      socket.close()
      log.debug("连接关闭...")
    case(404,socket:Socket)=>
      val out = new PrintWriter(socket.getOutputStream())
      out.println("HTTP/1.0 404 Not found");//返回应答消息,并结束应答
      out.println();// 根据 HTTP 协议, 空行将结束头信息
      out.close();
      socket.close()
  }
}
