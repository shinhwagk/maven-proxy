package org.gk.workers.down

import java.io.{RandomAccessFile, File}
import java.net.HttpURLConnection

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.routing.RoundRobinPool
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/28.
 */
class DownMaster(processNumber:Int,downManager:ActorRef) extends Actor with ActorLogging{

  val downWorker = context.actorOf(RoundRobinPool(processNumber).props(Props[DownWorker]),name ="downWorker")
  var downSuccessNumber:Int = _
  var fileOS:String = _
  override def receive: Receive = {
    case ("DownloadFile",fileUrl:String,fileOS:String) =>{
      this.fileOS = fileOS
      downFile (fileUrl,fileOS,processNumber)
    }
    case ("DownloasdSuccess") =>{
      downSuccessNumber += 1
      log.info("Worker下载完成数量{}/{}",downSuccessNumber,processNumber)
      if(downSuccessNumber == processNumber) downManager ! ("FileDownSuccess",fileOS)
    }
  }

  def downFile(fileUrl:String,fileOs:String,processNumber:Int): Unit ={
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
    raf.close()

    val endLength = fileLength%processNumber
    val step = (fileLength-endLength)/processNumber

    for (thread <- 1 to processNumber) {
      thread match {
        case _ if thread == processNumber =>{
          downWorker ! Work(fileUrl,thread,(thread - 1)*step,thread*step+endLength,fileOs)
          log.debug("线程: {} 下载请求已经发送...",thread)
        }
        case _ =>{
          downWorker ! Work(fileUrl,thread,(thread - 1)*step,thread*step-1,fileOs)
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
