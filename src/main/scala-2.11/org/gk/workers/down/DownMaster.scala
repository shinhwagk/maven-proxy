package org.gk.workers.down

import java.io.{RandomAccessFile, File}
import java.net.HttpURLConnection
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.down.DownWorker.Down

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.workers.down.DownMaster.{WorkDownSuccess, DownFile}
import slick.driver.H2Driver.api._
import slick.dbio.DBIO
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import scala.concurrent.Await
/**
 * Created by goku on 2015/7/28.
 */
class DownCount(val worksNum:Int,var successNum:Int)

object DownMaster {
  case class DownFile(fileUrl:String,file:String)
  case class WorkDownSuccess(url:String,file:String,startIndex:Int)
}
class DownMaster extends Actor with ActorLogging{

  var downManager:ActorRef = _

  //在启动时下载为下载完的部分
  Await.result(db.run(downFileWorkList.filter(_.success === 0).result).map(_.foreach {
    case (file, fileUrl, startIndex, enIndex, success) =>
      context.watch(context.actorOf(Props(new DownWorker(fileUrl,1,startIndex,enIndex,file)))) ! Down
  }),Duration.Inf)



  override val supervisorStrategy = OneForOneStrategy(){
    case _: Exception => Restart
  }

  override def receive: Receive = {

    case DownFile(fileUrl,file) =>
      context.setReceiveTimeout(15 seconds)
      downManager = sender()
      allocationWork (fileUrl,file)

    case WorkDownSuccess(url,file,startIndex) =>
      Await.result(db.run(downFileWorkList.filter(_.fileUrl === url).filter(_.startIndex === startIndex).map(p => (p.success)).update(1)), Duration.Inf)
      val name = sender().path.name
      import org.gk.db.DML._
      val wokerSuccessNumber = countDownSuccessNumber(url)
      val fileDownNumber = selectDownNumber(url)
      println("查看已经完成数量++++++++" +wokerSuccessNumber+"/"+fileDownNumber )
      context.unwatch(sender())
      context.stop(sender())
      println(name +"关闭  。。。。")
      if(wokerSuccessNumber == fileDownNumber){
        deleteDownWorker(url)
        deleteDownfile(url)
        val fileOS = cfg.getLocalRepoDir + file
        val fileTempOS = fileOS + ".DownTmp"

        println("下载完成啦")
        println(fileOS)
        println(fileTempOS)
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
