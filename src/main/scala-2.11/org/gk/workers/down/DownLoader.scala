package org.gk.workers.down

import java.io._
import java.net.{Socket, HttpURLConnection, URL}

import akka.actor.{Props, Actor}
import akka.routing.RoundRobinPool
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/22.
 */
class Downloader extends Actor with akka.actor.ActorLogging{
  val processNum = cfg.getDownFilePorcessNumber
  val worker = context.actorOf(RoundRobinPool(processNum).props(Props[Worker]),name ="worker")

  override def receive: Actor.Receive = {
    case (fileUrl:String,fileOs:String,socket:Socket) =>{
      downFile(fileUrl,fileOs)
      sender() ! ("DownSuccess",fileOs,socket)
    }
  }
  def downFile(fileUrl:String,fileOs:String): Unit ={
    val httpConn = getHttpConn(fileUrl)
    val fileLength = httpConn.getContentLength

    val file = new File(fileOs)
    if (!file.getParentFile.exists()) {
      file.getParentFile.mkdirs()
    }

    log.info("待下载文件{},需要下载{}...",fileOs,fileLength)

    log.info("定位在下文件{}...",fileOs)
    val raf = new RandomAccessFile(fileOs, "rwd");
    raf.setLength(fileLength);

    val endLength = fileLength%processNum
    val step = (fileLength-endLength)/processNum

    for (thread <- 1 to processNum) {
      thread match {
        case _ if thread == processNum =>{
          worker ! Work(fileUrl,thread,(thread - 1)*step,thread*step+endLength,raf)
          log.debug("线程: {} 下载请求已经发送...",thread)
        }
        case _ =>{
          worker ! Work(fileUrl,thread,(thread - 1)*step,thread*step-1,raf)
          log.debug("线程: {} 下载请求已经发送...",thread)
        }
      }
    }
  }
  def getHttpConn(Url:String): HttpURLConnection ={
    import java.net.{HttpURLConnection, URL};
    val downUrl = new URL(Url)
    downUrl.openConnection().asInstanceOf[HttpURLConnection];
  }
}

case class Work(url:String,thread:Int,startIndex:Int, endIndex:Int,raf:RandomAccessFile)
class Worker extends Actor with akka.actor.ActorLogging{
  override def receive: Actor.Receive = {
    case Work(url,thread,startIdex,endIndex,raf) => {
      log.debug("线程: {} 下载请求收到,开始下载...",thread)
      down(url,thread,startIdex,endIndex,raf)
      log.debug("线程: {} 下载完毕...",thread)
    };
  }

  def down(url:String,thread:Int,startIndex:Int, endIndex:Int,raf:RandomAccessFile) {
    log.debug("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url)
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
    val is = downConn.getInputStream();
    val workFileLength = downConn.getContentLength
    raf.seek(startIndex);

    var currentLength = 0
    var start = 0
    var len = 0

    val buffer = new Array[Byte](workFileLength)
    while (len != -1 && workFileLength != currentLength) {
      len = is.read(buffer, start, workFileLength - currentLength)
      start += len
      currentLength += len
      println(currentLength + "/" + workFileLength)
      log.info("线程: {}下载进度 {}/{} 下载完毕...",thread,currentLength,workFileLength)
    }
    raf.write(buffer)
    is.close()
    println("下载完毕")
  }
}

