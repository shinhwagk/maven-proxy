package org.gk.workers.down

import java.io.RandomAccessFile
import java.net.{URL, HttpURLConnection}

import akka.actor.{ActorLogging, Actor}
import org.gk.config.cfg
import org.gk.db.MetaData._
import org.gk.db.Tables._

import org.gk.workers.down.DownMaster.WorkDownSuccess
import org.gk.workers.down.DownWorker.Down

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by goku on 2015/7/28.
 */

object DownWorker{
  case object Down
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
    case Down => {
      log.info("线程: {} 下载{};收到,开始下载{}...",thread,url,fileTmpOS)
      down
      log.info("线程: {} 下载{};完毕{}...",thread,url,fileTmpOS)
    }
  }

  override def preStart: Unit ={
    down
  }

  def down = {
    log.info("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url);
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(2000)
    downConn.setReadTimeout(2000)
    downConn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
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

    log.info("线程:{},下载完毕",thread)
    log.info("WorkerDownLoadSuccess   {}   下载完成",self.path.name)
//    Await.result(db.run(downFileWorkList.filter(_.fileUrl === url).filter(_.startIndex === startIndex).map(p => (p.success)).update(1)), Duration.Inf)

    sender() ! WorkDownSuccess(url,file,startIndex)
  }


}