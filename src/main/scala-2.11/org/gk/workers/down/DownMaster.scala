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
class DownMaster(downManager:ActorRef) extends Actor with ActorLogging{
  val processNumber = cfg.getDownFilePorcessNumber
  val downWorker = context.actorOf(RoundRobinPool(processNumber).props(Props[DownWorker]),name ="downWorker")
  context.watch(downWorker)
  var downSuccessNumber:Int = _
  println("当前"+ downSuccessNumber)
  var fileOS:String = _
  var fileTmpOS:String = _
  override def receive: Receive = {
    case ("DownloadFile",fileUrl:String,file:String) =>{
      this.fileOS = cfg.getLocalRepoDir + file
      this.fileTmpOS = cfg.getLocalRepoTmpDir + file
      downFile (fileUrl,fileTmpOS,processNumber)
    }
    case ("WorkerDownLoadSuccess") =>{
      downSuccessNumber += 1
      log.info("Worker下载完成数量{}/{}",downSuccessNumber,processNumber)
      if(downSuccessNumber == processNumber) {
        downSuccessNumber = 0;
        val fileOS = new File(this.fileOS)
        if (!fileOS.getParentFile.exists()) {
          fileOS.getParentFile.mkdirs()
        }
        val fileTmpOS = new File(this.fileTmpOS)
        fileTmpOS.renameTo(fileOS)

        downManager ! ("FileDownSuccess",this.fileOS)
      }
    }
  }

  def downFile(fileUrl:String,fileTmpOS:String,processNumber:Int): Unit ={
    val httpConn = getHttpConn(fileUrl)
    val fileLength = httpConn.getContentLength

    val file = new File(fileTmpOS)
    if (!file.getParentFile.exists()) {
      file.getParentFile.mkdirs()
    }

    log.info("待下载文件{},需要下载{}...",fileTmpOS,fileLength)

    log.info("定位在下文件{}...",fileTmpOS)
    val raf = new RandomAccessFile(fileTmpOS, "rwd");
    raf.setLength(fileLength);
    raf.close()

    val endLength = fileLength%processNumber
    val step = (fileLength-endLength)/processNumber

    for (thread <- 1 to processNumber) {
      thread match {
        case _ if thread == processNumber =>{
          downWorker ! Work(fileUrl,thread,(thread - 1)*step,thread*step+endLength,fileTmpOS)
          log.debug("线程: {} 下载请求已经发送...",thread)
        }
        case _ =>{
          downWorker ! Work(fileUrl,thread,(thread - 1)*step,thread*step-1,fileTmpOS)
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
