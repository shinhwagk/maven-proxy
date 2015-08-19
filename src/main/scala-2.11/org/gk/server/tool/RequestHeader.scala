package org.gk.server.tool

import java.io.BufferedInputStream
import java.net.Socket

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/8/19.
 */
case class RequestHeader(s: Socket) {

  val socket = s

  private val bis = new BufferedInputStream(socket.getInputStream)

  private lazy val headerBytes: Array[Byte] = {
    val tempByteBuffer = new ArrayBuffer[Int]
    val dividingLine = ArrayBuffer(13, 10, 13, 10) //\n\r
    while (tempByteBuffer.takeRight(4) != dividingLine) {
      tempByteBuffer += bis.read()
    }
    tempByteBuffer.trimEnd(4)
    tempByteBuffer.map(_.toByte).toArray
  }

  lazy val headerString: String = {
    new String(headerBytes)

  }

  lazy val headerList: List[String] = headerString.split("\r\n").toList

  lazy val filePath = headerString.split("\r\n")(0).split(" ")(1)
}