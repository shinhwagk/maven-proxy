package org.gk.workers.down

import java.io.RandomAccessFile
import java.net.{URL, HttpURLConnection}

import akka.actor.{ActorLogging, Actor}

/**
 * Created by goku on 2015/7/28.
 */

class DownWorker(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOsTmp:String) extends Actor with ActorLogging{
  override def receive: Actor.Receive = {
    case Work => {
      log.debug("线程: {} 下载请求收到,开始下载{}...",thread,fileOsTmp)
//      sender() !
        down()
      log.debug("线程: {} 下载完毕{}...",thread,fileOsTmp)
    }
  }
  override def preStart: Unit ={
    down()
  }

  def down():Unit = {
    log.debug("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url);
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000)
    downConn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
    val is = downConn.getInputStream();
    val workFileLength = downConn.getContentLength;
    val raf = new RandomAccessFile(fileOsTmp, "rwd");
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
      log.info("线程: {};下载文件{}，进度 {}/{} ...",thread,url,currentLength,workFileLength)
      log.debug("线程: {};下载文件{}，进度 {}/{} ...",thread,url,currentLength,workFileLength)

    }

    raf.write(buffer)
    is.close()
    raf.close()
    log.info("线程:{},下载完毕",thread)
    log.info("WorkerDownLoadSuccess   {}   下载完成",self.path.name)
  }
}