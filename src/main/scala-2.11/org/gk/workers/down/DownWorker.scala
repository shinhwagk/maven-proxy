package org.gk.workers.down

import java.io.RandomAccessFile
import java.net.{URL, HttpURLConnection}

import akka.actor.Actor

/**
 * Created by goku on 2015/7/28.
 */

class DownWorker extends Actor with akka.actor.ActorLogging{
  override def receive: Actor.Receive = {
    case Work(url,thread,startIdex,endIndex,fileOs) => {
      log.debug("线程: {} 下载请求收到,开始下载{}...",thread,fileOs)
      sender() ! down(url,thread,startIdex,endIndex,fileOs)
      log.debug("线程: {} 下载完毕{}...",thread,fileOs)
    }
  }

  def down(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOs:String):String = {
    log.debug("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url);
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
    val is = downConn.getInputStream();
    val workFileLength = downConn.getContentLength;
    val raf = new RandomAccessFile(fileOs, "rwd");
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
    raf.close()
    println("下载完毕")
    "DownloasdSuccess"
  }
}