package org.gk.proxy

import java.io.{InputStream, OutputStream}

import akka.actor.Actor

/**
 * Created by goku on 2015/8/25.
 */
class ProxyTypeConnectClient extends Actor {
  override def receive: Receive = {
    case (in: InputStream, out: OutputStream) =>
      println("客户端发送给服务点")
      var buffer = in.read()
      while (true) {
        println("客户端发送给服务点" + buffer)
        out.write(buffer)
        out.flush()
        buffer = in.read()

        println("客户端" + buffer)

      }
      println("客户端发送给服务点结束:" + buffer)
  }
}
