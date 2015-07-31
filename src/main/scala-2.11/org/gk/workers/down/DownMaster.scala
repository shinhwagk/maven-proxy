package org.gk.workers.down

import java.io.{RandomAccessFile, File}
import java.net.HttpURLConnection

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.routing.RoundRobinPool
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/28.
 */
object WorkerActorDownDB{
  var actorIdMap = Map[String, Work]()
  var actorId = 0;
  def saveDownMap(a:String, b:Work) = synchronized {
    actorIdMap += (a -> b)
  }
  def load(key: String): Work = synchronized {
    actorIdMap(key)
  }
  def getActorId:Int  = synchronized{
    actorId += 1
    actorId
  }
}
object WorkDownFileDB{
  var downFileMap = Map[String, Map[String,Work]]()

  def saveDownMap(fileName:String,actorName:String,work:Work):Unit = synchronized {
//    downFileMap(fileName) += (actorName ->work)

  }
  def load(key: String): Map[String,Work] = synchronized {
    downFileMap(key)
  }

  def remove(key:String): Unit = synchronized{
    downFileMap -= key
  }
}

class DownMaster(downManager:ActorRef) extends Actor with ActorLogging{
  import WorkerActorDownDB._
//  val downWorker = context.actorOf(RoundRobinPool(processNumber).props(Props[DownWorker]),name ="downWorker")
//  context.watch(downWorker)
  var fileOS:String = _
  var fileTmpOS:String = _

  override val supervisorStrategy = OneForOneStrategy(){
    case _: Exception => Stop
  }

  override def receive: Receive = {
    case ("DownloadFile",fileUrl:String,file:String) =>{
      this.fileOS = cfg.getLocalRepoDir + file
      this.fileTmpOS = cfg.getLocalRepoTmpDir + file
      allocationWork (fileUrl,fileTmpOS,1)
    }
    case ("WorkerDownLoadSuccess") =>{
        downManager ! ("FileDownSuccess",this.fileOS)
    }
    case Terminated(actorRef) =>{
      for((k,v) <- actorIdMap){
        println("查看当前map数量  "+ k)
      }
      println(actorRef.path.name+"被关闭")
      val actorRefName = actorRef.path.name +"_"+getActorId
      context.watch(context.actorOf(Props[DownWorker],name = actorRefName)) ! actorIdMap(actorRefName)
    }
  }

  def allocationWork(fileUrl:String,fileTmpOS:String,processNumber:Int): Unit ={
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
          val actorName = "downWoker_"+ getActorId
          saveDownMap(actorName ,Work(fileUrl,thread,(thread - 1)*step,thread*step+endLength,fileTmpOS))
          context.watch(context.actorOf(Props(new DownWorker(fileUrl,thread,(thread - 1)*step,thread*step+endLength,fileTmpOS)),name = actorName)) ! Work
          //            downWorker ! Work(fileUrl,thread,(thread - 1)*step,thread*step+endLength,fileTmpOS)
          log.debug("线程: {} 下载请求已经发送...",thread)
        }
        case _ =>{
          val actorName = "downWoker_"+ getActorId
          saveDownMap(actorName , Work(fileUrl,thread,(thread - 1)*step,thread*step-1,fileTmpOS))
//          downWorker ! Work(fileUrl,thread,(thread - 1)*step,thread*step-1,fileTmpOS)
          context.watch(context.actorOf(Props(new DownWorker(fileUrl,thread,(thread - 1)*step,thread*step-1,fileTmpOS)),name = actorName)) ! Work
          //
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
