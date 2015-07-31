package org.gk.workers.down

import java.io.{RandomAccessFile, File}
import java.net.HttpURLConnection
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
//object WorkerActorDownDB{
//  var actorIdMap = Map[String, Work]()
//  var actorId = 0;
//  def saveDownMap(a:String, b:Work) = synchronized {
//    actorIdMap += (a -> b)
//  }
//  def load(key: String): Work = synchronized {
//    actorIdMap(key)
//  }
//  def getActorId:Int  = synchronized{
//    actorId += 1
//    actorId
//  }
//}
//object WorkDownFileDB{
//  var downFileMap = Map[String, Map[String,Work]]()
//
//  def saveDownMap(fileName:String,actorName:String,work:Work):Unit = synchronized {
////    downFileMap(fileName) += (actorName ->work)
//
//  }
//  def load(key: String): Map[String,Work] = synchronized {
//    downFileMap(key)
//  }
//
//  def remove(key:String): Unit = synchronized{
//    downFileMap -= key
//  }
//}
class DownCount(val worksNum:Int,var successNum:Int)
object downFileSuccess {
  var a = Map[String,DownCount]()

  def addFileUrl(fileUrl:String,downCount:DownCount) = synchronized{
      a = Map(fileUrl -> downCount)
  }

  def addOnceFileWorkSuccess(fileUrl:String) = synchronized{
    val b = a(fileUrl)
    b.successNum +=1
    println(fileUrl +  b.successNum)
  }

  def getWorksNum(fileUrl:String) = synchronized{
    val b = a(fileUrl)
    b.worksNum
  }
  def getSuccessNum(fileUrl:String) = synchronized {
    val b = a(fileUrl)
    b.successNum
  }

  def getProgress(fileUrl:String) = synchronized{
    println("进度"+getSuccessNum(fileUrl)+"/"+getWorksNum(fileUrl))
  }
}


object DownMaster {
  case class DownFile(fileUrl:String,file:String)
  case class WorkDownSuccess(url:String)
}
class DownMaster(downManager:ActorRef) extends Actor with ActorLogging{

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10,
    withinTimeRange = 300 seconds){
    case _: Exception => Restart
  }

  override def receive: Receive = {

    case DownFile(fileUrl,file) =>
      val fileTmpOS = cfg.getLocalRepoTmpDir + file
      allocationWork (fileUrl,fileTmpOS)

    case WorkDownSuccess(url) =>
      downFileSuccess.addOnceFileWorkSuccess(url)
      downFileSuccess.getProgress(url)
//      context.stop(sender())

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

  def allocationWork(fileUrl:String,fileTmpOS:String): Unit ={
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
    downFileSuccess.addFileUrl(fileUrl,downaa)



    for (thread <- 1 to workNumber) {
      thread match {
        case _ if thread == workNumber =>{
          context.watch(context.actorOf(Props(new DownWorker(fileUrl,thread,(thread - 1)*step,thread*step+endLength,fileTmpOS)))) ! Work
          log.info("线程: {} 下载请求已经发送...",thread)
        }
        case _ =>{
          context.watch(context.actorOf(Props(new DownWorker(fileUrl,thread,(thread - 1)*step,thread*step-1,fileTmpOS)))) ! Work
          log.info("线程: {} 下载请求已经发送...",thread)

        }
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
  def createTmpfile(fileTmpOS:String,fileLength:Int): Unit ={
    val file = new File(fileTmpOS)
    if (!file.getParentFile.exists()) {
      file.getParentFile.mkdirs()
    }
    val raf = new RandomAccessFile(fileTmpOS, "rwd");
    raf.setLength(fileLength);
    raf.close()

    log.info("创建临时文件{}...",fileTmpOS)
  }
}
