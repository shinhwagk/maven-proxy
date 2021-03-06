package org.gk.server.workers

import java.io._
import java.net.{HttpURLConnection, InetSocketAddress, Socket, URL}

import akka.actor.Actor

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/27.
 */

object HeadParser {

  case class RequertFilePath(socket: Socket)

}


class HeadParser extends Actor with akka.actor.ActorLogging {

  import HeadParser._

  override def receive: Receive = {
    case RequertFilePath(socket: Socket) => {
//      sender() ! StoreRequert(getFilePath(socket), socket)
    }
  }

  def getHeadInfo(socket: Socket): Map[String, String] = {
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    var a: Map[String, String] = Map.empty
    var templine = br.readLine()
    //    val b = templine.split(" ")
    //    a += ("PATH" -> b(1))

    templine = br.readLine()

    while (templine != null && templine != "") {
      println(templine)
      val b = templine.split(":")

      b(0) match {
        case "Connection" => a += (b(0) -> b(1))
        case _ => None
      }
      templine = br.readLine()
    }

    a
  }

  def getFilePath(socket: Socket): String = {
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val templine = br.readLine()
    println(templine)
    val b = templine.split(" ")
    b(1)
  }
}

class RequestHeaders(s: Socket) {
  val socket = s
  val bis = new BufferedInputStream(socket.getInputStream)

  lazy val Head_Date = getHeader("Date")
  lazy val Head_Server = getHeader("Server")
  lazy val Head_HttpVersion = {
    if (Head_First.startsWith("GET"))
      Head_First.split(" ")(2)
  }
  lazy val Head_HttpResponseCode = Head_First.split(" ")(1)
  lazy val Head_HttpResponseString = Head_First.split(" ")(2)
  lazy val Head_First = headText.split("\r\n")(0)
  lazy val Head_Method = {
    if (Head_First.startsWith("GET"))
      "GET"
  }
  lazy val Head_Path: Option[String] = {
    if (Head_First.startsWith("GET") || Head_First.startsWith("HEAD"))
      Some(Head_First.split(" ")(1))
    else None
  }
  lazy val Head_ContentType = getHeader("Content-Type")
  lazy val Head_AcceptRanges = getHeader("Accept-Ranges")
  lazy val Head_ContentLength = getHeader("Content-Length")
  lazy val Head_ContentRange = getHeader("Content-Range")
  lazy val Head_SetCookie = getHeader("Set-Cookie")
  lazy val Head_Via = getHeader("Via")
  lazy val Head_Connection = getHeader("Connection")
  lazy val Head_Cachecontrol = getHeader("Cache-control")
  lazy val Head_Cachestore = getHeader("Cache-store")
  lazy val Head_Pragma = getHeader("Pragma")
  lazy val Head_Expires = getHeader("Expires")
  lazy val Head_AcceptEncoding = getHeader("Accept-Encoding")
  lazy val Head_UserAgent = getHeader("User-Agent")

  def getHeader(par: String): Option[String] = {
    val a = headText.split("\r\n")
    val headSeq = for (i <- 1 to a.length - 1) yield {
      val cc = a(i).split(": "); (cc(0) -> cc(1))
    }
    val headMap = headSeq.toMap
    headMap.get(par)
  }

  lazy val headText = {
    val tempByteBuffer = new ArrayBuffer[Byte]
    val dividingLine = ArrayBuffer(13, 10, 13, 10)
    var byteData = 0
    var stopMark = true
    while (stopMark != false && byteData != -1) {
      byteData = bis.read()
      tempByteBuffer += byteData.toByte
      if (tempByteBuffer.length >= 4 && tempByteBuffer.takeRight(4) == dividingLine) {
        stopMark = false
        tempByteBuffer.trimEnd(2);
      }
    }
    val a = new String(tempByteBuffer.toArray)
    //    println(a)
    a
  }
}

object abc {
  def main(args: Array[String]) {
    val b = "http://repository.apache.org/content/groups/snapshots/org/apache/geode/gemfire-core/1.0.0-incubating-SNAPSHOT/maven-metadata.xml"
    ////    val b = "http://127.0.0.1:9995/apache-snapshots/org/apache/geode/gemfire-core/1.0.0-incubating-SNAPSHOT/gemfire-core-1.0.0-incubating-20150813.110411-49.jar"
    //    val url = new URL(b);
    //    val host = url.getHost();
    //    val port = url.getDefaultPort()
    //    println(url.getPort())
    //    println("Host Name = " + host);
    //    println("port = " + port);
    //    println("File URI = " + url.getFile());
    //    println(" xx");
    //
    //    val socket = new Socket();
    //    val address = new InetSocketAddress(host, 80);
    //    socket.connect(address);
    //    val bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
    //    bufferedWriter.write("HEAD " + url.getFile() + " HTTP/1.1\r\n"); // 请求头信息发送结束标志
    ////    bufferedWriter.write("Accept-Encocmd
    // ding: gzip\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("Connection: Keep-Alive\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("Expires: 0\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("Pragma: no-cache\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("Cache-store: no-store\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("Cache-control: no-cache\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("Host: " + host + "\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("Range: bytes=10-19\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.write("\r\n"); // 请求头信息发送结束标志
    //    bufferedWriter.flush()
    //    val aa = new RequestHeaders(socket)
    //    println(aa.headText)
    //    println(aa.Head_ContentLength)
    //
    //    print(new String(Array(socket.getInputStream.read().toByte)))
    //    print(new String(Array(socket.getInputStream.read().toByte)))
    //    print(new String(Array(socket.getInputStream.read().toByte)))
    //    print(new String(Array(socket.getInputStream.read().toByte)))
    val httpConn = new URL(b).openConnection.asInstanceOf[HttpURLConnection]
    httpConn.setConnectTimeout(10000)
    httpConn.setReadTimeout(10000)
    httpConn.setRequestProperty("Cache-Control", "no-cache")
    httpConn.setRequestProperty("Expires", "0")
    httpConn.setRequestProperty("Pragma", "no-cache")
    httpConn.setRequestProperty("Range", "bytes=0-1")
    httpConn.setRequestProperty("Cache-store", "no-store");
    println(httpConn.getHeaderFields.get("Content-Range").get(0))
  }
}


