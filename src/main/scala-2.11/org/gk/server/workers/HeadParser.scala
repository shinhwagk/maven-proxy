package org.gk.server.workers

import java.io._
import java.net.{HttpURLConnection, InetSocketAddress, URL, Socket}

import akka.actor.Actor
import org.gk.server.workers.Doorman.StoreRequert
import org.gk.server.workers.RepoManager.RequertFile

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
      sender() ! StoreRequert(getFilePath(socket), socket)
    }
  }


  def getHeadInfo(socket: Socket): Map[String, String] = {
    val br = new BufferedReader(new InputStreamReader(socket.getInputStream))
    var a: Map[String, String] = Map.empty
    var templine = br.readLine()
    println("xxxxxxxxxxxxxxxxx")
    println(templine + "    head")
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
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    println(br.readLine())
    b(1)
  }


}

class Headers(s: Socket) {
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
    println(Head_First)
    if (Head_First.startsWith("GET"))
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
    new String(tempByteBuffer.toArray)
  }

  def setHeader(key :String,value:String)={

  }
}

object abc {
  def main(args: Array[String]) {
    val b = "https://repository.apache.org/content/groups/snapshots/org/apache/geode/gemfire-core/1.0.0-incubating-SNAPSHOT/maven-metadata.xml"
    val url = new URL(b);
    val host = url.getHost();
    val port = url.getDefaultPort()
    println(url.getPort())
    println("Host Name = " + host);
    println("port = " + port);
    println("File URI = " + url.getFile());
    println(" xx");

    val socket = new Socket();
    val address = new InetSocketAddress(host, 80);
    socket.connect(address);
    val bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
    bufferedWriter.write("GET " + url.getFile() + " HTTP/1.1\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("ContentType: application/octet-stream\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("Range: bytes=0-814\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("Host: " + host + "\r\n"); // 请求头信息发送结束标志
    bufferedWriter.write("\r\n"); // 请求头信息发送结束标志
    bufferedWriter.flush()
    val aa = new Headers(socket)
    println(aa.headText)
    println(aa.Head_HttpResponseCode + "xxxxxx1111111111")
    println(aa.Head_AcceptRanges + "xxxxxx1111111111")
    println(aa.Head_ContentLength + "xxxxxx1111111111")
    println(aa.Head_Server + "xxxxxx1111111111")
    println(aa.Head_Path + "xxxxxx1111111111")
    println(aa.Head_First + "xxxxxx1111111111")


    println("aaaa")
    val downUrl = new URL(b);

    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000)
    downConn.setReadTimeout(10000)
//    downConn.setRequestProperty("ContentType", "application/octet-stream");
//    downConn.setRequestProperty("Accept", "*/*");
//    downConn.setRequestProperty("Range", "bytes=" + 0 + "-" + 800);
//    downConn.connect()
//    println(downConn.getResponseCode)
    println(downConn.getContentLength)
  }
}