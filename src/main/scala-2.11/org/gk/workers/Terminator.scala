package org.gk.workers

import java.io.BufferedOutputStream

import akka.actor.Actor
import akka.actor.Actor.Receive
import java.net.Socket;

/**
 * Created by gk on 15/7/26.
 */
class Terminator extends Actor{
  override def receive: Receive = {
    case parameter:(Int,Socket) => {
      closeCode(parameter)
    }
    case socket:Socket =>{
      socket.close()
    }
  }

  def closeCode(par:(Int,Socket)) = {
    par._1 match {
      case 204 => close_204(par._2);println("关闭无效链接")
      case 200 => par._2.close()
    }
  }
  def close_204(socket:Socket) = {
    val bos = new BufferedOutputStream(socket.getOutputStream);
    val sb = new StringBuilder();
    sb.append("HTTP/1.1 204 OK\n");
    sb.append("\n");
    bos.write(sb.toString().getBytes);
    bos.flush();
  }
}
