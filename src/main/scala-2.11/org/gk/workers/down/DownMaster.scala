package org.gk.workers.down

import java.io.{RandomAccessFile, File}
import java.net.HttpURLConnection
import org.gk.workers.down.DownWorker.Down

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.workers.down.DownMaster.{WorkDownSuccess, DownFile}

/**
 * Created by goku on 2015/7/28.
 */
class DownCount(val worksNum:Int,var successNum:Int)

object DownMaster {
  case class DownFile(fileUrl:String,file:String)
  case class WorkDownSuccess(url:String,file:String)
}
class DownMaster(downManager:ActorRef) extends Actor with ActorLogging{

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10,
    withinTimeRange = 300 seconds){
    case _: Exception => Restart
  }

  override def receive: Receive = {

    case DownFile(fileUrl,file) =>
      allocationWork (fileUrl,file)

    case WorkDownSuccess(url,file) =>
      val name = sender().path.name
      import org.gk.db.DML._
      val wokerSuccessNumber = countDownSuccessNumber(url)
      val fileDownNumber = selectDownNumber(url)
      println("完成数量++++++++" +wokerSuccessNumber )
      println("应该完成数量++++++++" + wokerSuccessNumber )
      println("查看worknumber " +selectDownNumber(url))
      context.unwatch(sender())
      context.stop(sender())
      println(name +"关闭  。。。。")
      if(wokerSuccessNumber == fileDownNumber){
        import org.gk.db.MetaData._
        import org.gk.db.Tables._
        deleteDownWorker(url)
        deleteDownfile(url)
        val fileOS = cfg.getLocalRepoDir + file
        val fileTempOS = fileOS + ".DownTmp"
        val fileOSHeadle = new File(fileOS);
        val fileTempOSHeadle = new File(fileTempOS);
        fileTempOSHeadle.renameTo(fileOSHeadle)
      }

//    case ("WorkerDownLoadSuccess") =>{
//        downManager ! ("FileDownSuccess",this.fileOS)
//    }
//    case Terminated(actorRef) =>{
//      for((k,v) <- actorIdMap){
//        println("查看当前map数量  "+ k)
//      }
//      println(actorRef.path.name+"被关闭")
//      val actorRefName = actorRef.path.name +"_"+getActorId
//      context.watch(context.actorOf(Props[DownWorker],name = actorRefName)) ! actorIdMap(actorRefName)
//    }
  }

  def allocationWork(fileUrl:String,file:String): Unit ={

    val fileTmpOS = cfg.getLocalRepoDir+file+".DownTmp"
    val httpConn = getHttpConn(fileUrl)
    val fileLength = httpConn.getContentLength

    val workNumber = getWorkNum(fileLength)

    log.info("待下载文件{},需要下载 {},需要线程数量{}...",fileUrl,fileLength,workNumber)
    log.info("定位在下文件{}...",fileTmpOS)

    //创建临时文件需要的目录和文件
    createTmpfile(fileTmpOS,fileLength)

    val endLength = fileLength % workNumber

    //步长
    val step = (fileLength-endLength)/workNumber

    val downaa = new DownCount(workNumber,0)

    import org.gk.db.DML._
    insertDownMaster(file,fileUrl,workNumber)

    for (thread <- 1 to workNumber) {
      thread match {
        case _ if thread == workNumber =>
          val startIndex = (thread - 1)*step
          val endIndex = thread*step+endLength
          insertDownWorker(file,fileUrl,startIndex,endIndex,0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl,thread,startIndex,endIndex,file)))) ! Down
          log.info("线程: {} 下载请求已经发送...",thread)

        case _ =>
          val startIndex = (thread - 1)*step
          val endIndex = thread*step+endLength-1
          insertDownWorker(file,fileUrl,startIndex,endIndex,0)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl,thread,startIndex,endIndex,file)))) ! Down
          log.info("线程: {} 下载请求已经发送...",thread)


      }
    }
  }
  def getHttpConn(Url:String): HttpURLConnection ={
    import java.net.{HttpURLConnection, URL};
    val downUrl = new URL(Url)
    downUrl.openConnection().asInstanceOf[HttpURLConnection];
  }

  def getWorkNum(fileLength:Int): Int = {
    val processForBytes = cfg.getPerProcessForBytes
    fileLength / processForBytes
  }
  def createTmpfile(fileTmpOS:String,fileLength:Int): Unit = {
    val file = new File(fileTmpOS)
    if (!file.getParentFile.exists()) {
      file.getParentFile.mkdirs()
    }
    val raf = new RandomAccessFile(fileTmpOS, "rwd");
    raf.setLength(fileLength);
    raf.close()

    log.info("创建临时文件{}...",fileTmpOS)
  }

  def renameFile(fileTemp:String): Unit = {

  }
}
