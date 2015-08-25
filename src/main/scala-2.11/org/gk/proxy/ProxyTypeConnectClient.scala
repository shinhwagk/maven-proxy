package org.gk.proxy

import java.io.{InputStream, OutputStream}

import akka.actor.Actor

/**
 * Created by goku on 2015/8/25.
 */
class ProxyTypeConnectClient extends Actor {
  override def receive: Receive = {
    case (clientIn: InputStream, out: OutputStream) =>
      println("客户端发送给服务点")
      var buffer = clientIn.read()
      while (true) {
        println("客户端发送给服务点" + buffer)
        out.write(buffer)
        out.flush()
        buffer = clientIn.read()
        println(clientIn.available() + "客户端可用")

      }
      println("客户端发送给服务点结束:" + buffer)
  }
}
