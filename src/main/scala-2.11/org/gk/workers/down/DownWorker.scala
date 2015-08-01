package org.gk.workers.down

import java.io.RandomAccessFile
import java.net.{URL, HttpURLConnection}

import akka.actor.{ActorLogging, Actor}
import org.gk.workers.down.DownMaster.WorkDownSuccess
import org.gk.workers.down.DownWorker.Down

/**
 * Created by goku on 2015/7/28.
 */

object DownWorker{
  case object Down{}
}
class DownWorker(url:String,thread:Int,startIndex:Int, endIndex:Int,fileOsTmp:String) extends Actor with ActorLogging{
  override def receive: Actor.Receive = {
    case Down => {
      log.info("线程: {} 下载{};收到,开始下载{}...",thread,url,fileOsTmp)
        down
      log.info("线程: {} 下载[];完毕{}...",thread,url,fileOsTmp)
    }
  }

  override def preStart: Unit ={
    down
  }


  def down = {
    log.info("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url);
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000)
    downConn.setReadTimeout(2000)
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
      log.info("{}下载完成进度:{}/{}",url,currentLength, workFileLength)
      log.debug("线程: {};下载文件{}，进度 {}/{} ...",thread,url,currentLength,workFileLength)
    }

    raf.write(buffer)
    is.close()
    raf.close()
    log.info("线程:{},下载完毕",thread)
    log.info("WorkerDownLoadSuccess   {}   下载完成",self.path.name)
    sender() ! WorkDownSuccess(url)
  }
}