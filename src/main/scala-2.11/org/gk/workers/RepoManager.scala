package org.gk.workers

import java.io.{RandomAccessFile, File}

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import org.gk.config.cfg
import java.net.Socket

import org.gk.log.GkConsoleLogger
import org.gk.workers.down.Downloader

import scala.collection.mutable.ArrayBuffer

/**
 * Created by gk on 15/7/26.
 */
class RepoManager extends Actor with akka.actor.ActorLogging{
  val senderr = context.actorOf(Props[Sender],name ="Sender")
  val terminator = context.actorOf(Props[Terminator],name = "terminator")
  val downLoader = context.actorOf(Props[Downloader], name = "downLoader")

  override def receive: Receive = {
    case (file:String,socket:Socket) =>{

      val osFile = cfg.getLocalRepoDir + file
      val osFileHandle = new File(osFile)
      osFileHandle.exists() match {
        case true => {
          log.debug("文件:{} 存在本地...",osFile)
        }
        case false => {
          log.debug("文件:{} 不在本地...",osFile)
          downLoader ! (getFileUrl(file),osFile,socket)
//          getFile(file,socket)
        }
      }
    }
    case ("DownSuccess",fileOs:String,socket:Socket) =>{
      senderr ! (fileOs,socket)
    }
  }

  def getFile(file:String,socket:Socket): Unit ={
    val osFile = cfg.getLocalRepoDir + file
    val osFileHandle = new File(osFile)
    if(!osFileHandle.exists()){
      downFile(getFileUrl(file),osFile)
    }
  }
  def downFile(fileUrl:String,osFile:String): Unit ={
    import java.net.{HttpURLConnection, URL};
    val downUrl = new URL(fileUrl)
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000);
    val fileLength = downConn.getContentLength
    val downIs = downConn.getInputStream();

    val file = new File(osFile)
    if (!file.getParentFile.exists()) {
      file.getParentFile.mkdirs()
    }
    val raf = new RandomAccessFile(osFile, "rwd");
    val buffer = new Array[Byte](fileLength)
    raf.setLength(fileLength);


    var tot = 0
    var start = 0
    var len = 0
    while (len != -1 && fileLength != tot) {
      len = downIs.read(buffer, start, fileLength - tot)
      start += len
      tot += len
      println(tot+"/"+fileLength)
    }
    raf.write(buffer)
    downIs.close()
    raf.close();
    println("下载完毕")
  }

  def getFileUrl(file:String): String ={
    val remoteRepMap = cfg.getRemoteRepoMap
    val getRemoteRepo_Central = cfg.getRemoteRepoCentral
    val testCentralFileUrl = getRemoteRepo_Central + file
    println(testCentralFileUrl)
    println(getTestFileUrlCode(testCentralFileUrl))
    val fileUrl = if(getTestFileUrlCode(testCentralFileUrl) == 200 ){
      testCentralFileUrl
    }else{
      val a = remoteRepMap.filter(repo => (getTestFileUrlCode(repo._2+file) == 200))
      val b = a.map(x => x._2)
      val c = b.asInstanceOf[ArrayBuffer[String]]
      c(0)+file
    }
    fileUrl
  }
  def getTestFileUrlCode(fileUrl:String): Int ={
    import java.net.{HttpURLConnection, URL};
    val downUrl = new URL(fileUrl)
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000)
    downConn.getResponseCode
  }
}
