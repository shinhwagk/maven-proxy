package org.gk.workers

import java.io.{RandomAccessFile, File}
import java.net.Socket

import akka.actor.{Props, Actor}
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/24.
 */


class Doorman extends Actor {
  val terminator = context.actorOf(Props[Terminator])
  val headParser = context.actorOf(Props[HeadParser], name = "HeadParser")

  override def receive: Receive = {
    case socket: Socket => {
      val downFileInfo = DownFileInfo(socket)
      headParser ! RequertParserHead(downFileInfo)
      println("requert发送者接受到请求，准备处理...")
      println("requert发送者发出请求...")

    }
  }
}

case class DownFileInfo(s:Socket){

  val socket:Socket = s

  var file:String = _

  var fileUrl:String = _

  lazy val fileOS: String = cfg.getLocalRepoDir + file

  lazy val fileTempOS: String = fileOS + ".DownTmp"

  lazy val fileLength :Int = getFileLength

  lazy val workerNumber: Int = getDownWokerNumber
  
  lazy val workerDownInfo:Map[Int,(Int,Int)] = getwokerDownInfo

  private def getDownWokerNumber: Int = {
    val processForBytes = cfg.getPerProcessForBytes
    if (fileLength >= processForBytes) fileLength / processForBytes else 1
  }

  private def getFileLength: Int = {
    import java.net.{HttpURLConnection, URL};
    val conn = new URL(fileUrl).openConnection().asInstanceOf[HttpURLConnection];
    conn.setConnectTimeout(10000)
    conn.setReadTimeout(10000)
    val fileLength = conn.getContentLength
    conn.disconnect()
    fileLength
  }

  def createTmpfile: Unit = {
    val file = new File(fileTempOS)

    if (!file.getParentFile.exists) file.getParentFile.mkdirs()

    if(!file.exists) {
      val raf = new RandomAccessFile(fileTempOS, "rwd");
      raf.setLength(fileLength);
      raf.close()
    }
  }

  def getwokerDownInfo:Map[Int,(Int,Int)] = {
    val endLength = fileLength % workerNumber
    val step = (fileLength - endLength) / workerNumber
    var tempMap:Map[Int,(Int,Int)] = Map.empty
    for (i <- 1 to workerNumber) {
      val startIndex: Int = (i - 1) * step
      val endIndex = if (i == workerNumber) i * step + endLength else i * step - 1
      tempMap += (i -> (startIndex,endIndex))
    }
    tempMap
  }
}

