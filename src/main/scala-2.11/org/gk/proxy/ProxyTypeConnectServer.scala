package org.gk.proxy

import java.io.{InputStream, OutputStream}

import akka.actor.Actor

/**
 * Created by goku on 2015/8/25.
 */
class ProxyTypeConnectServer extends Actor {
  override def receive: Receive = {
    case (in: InputStream, out: OutputStream) =>
      println("服务点发送给客户端")
      var buffer = in.read()
      while (true) {
        println("服务点发送给客户端" + buffer)
        out.write(buffer)
        out.flush()
        buffer = in.read()

        println("服务端" + buffer)

      }
      println("服务点发送给服务点结束:" + buffer)
  }
}
