package org.gk.workers.down

import java.io.RandomAccessFile
import java.net.{URL, HttpURLConnection}

import akka.actor.{ActorLogging, Actor}
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._

import org.gk.workers.down.DownMaster.WorkDownSuccess
import org.gk.workers.down.DownWorker.Downming

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.io.{RandomAccessFile, File}
import java.net.HttpURLConnection
import org.gk.db.MetaData._
import org.gk.db.Tables._
import org.gk.workers.down.DownWorker.Downming

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.routing.RoundRobinPool
import org.gk.config.cfg
import org.gk.workers.down.DownMaster._
import slick.driver.H2Driver.api._
import slick.dbio.DBIO
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import scala.concurrent.Await
/**
 * Created by goku on 2015/7/28.
 */

object DownWorker{
  case object Downming
  def storeWorkFile(fileTempOS:String,startIndex:Int,buffer:Array[Byte]) = synchronized{
    val raf = new RandomAccessFile(fileTempOS, "rwd");
    raf.seek(startIndex);
    raf.write(buffer)
    raf.close()
  }
}
class DownWorker(url:String,thread:Int,startIndex:Int, endIndex:Int,file:String) extends Actor with ActorLogging{
  val fileTmpOS = cfg.getLocalRepoDir+file+".DownTmp"
  override def receive: Actor.Receive = {
    case Downming => {
//      log.info("线程: {} 下载{};收到,开始下载{}...",thread,url,fileTmpOS)
      down
//      log.info("线程: {} 下载{};完毕{}...",thread,url,fileTmpOS)
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    down
    println("actor:" + self.path + ",preRestart child, reason:" + reason + ", message:" + message)
  }

  def down = {
//    log.info("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url);
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000)
    downConn.setReadTimeout(5000)
    downConn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
    downConn.setRequestProperty("Accept-Encoding","gzip")
    val is = downConn.getInputStream();
    val workFileLength = downConn.getContentLength;


    var currentLength = 0
    var start = 0
    var len = 0

    val buffer = new Array[Byte](workFileLength)

    while (len != -1 && workFileLength != currentLength) {
      len = is.read(buffer, start, workFileLength - currentLength)
      start += len
      currentLength += len
//      log.info("{}下载完成进度:{}/{}",url,currentLength, workFileLength)
//      log.debug("线程: {};下载文件{}，进度 {}/{} ...",thread,url,currentLength,workFileLength)
    }


    import DownWorker._
    storeWorkFile(fileTmpOS,startIndex,buffer)
    is.close()

//    log.info("线程:{},下载完毕",thread)
//    log.info("WorkerDownLoadSuccess   {}   下载完成",self.path.name)
//    Await.result(db.run(downFileWorkList.filter(_.fileUrl === url).filter(_.startIndex === startIndex).map(p => (p.success)).update(1)), Duration.Inf)
//    sender() ! "aaaaa"
    //本work下载完毕,更新数据库

    sender() ! WorkDownSuccess(url,file,startIndex)
  }


}