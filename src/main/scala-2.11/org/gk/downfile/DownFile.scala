package org.gk.downfile

import java.io._
import java.net.{HttpURLConnection, URL}

import akka.actor.Actor
import org.gk.config.cfg
import org.gk.log.GkConsoleLogger

import scala.collection.mutable.ArrayBuffer

/**
 * Created by goku on 2015/7/22.
 */

class DownFile extends Actor{
  def downMaster(processNumber:Int,filePath:String){}
  def downWork(path:String, threadId:Int, startIndex:Int, endIndex:Int){}
  def getFileDownUrl(filePath:String):String ={
    val repositoryMap = org.gk.config.cfg.getRepositoryMap
    val a = repositoryMap.filter(repo => (testRepo(repo._2+filePath) == 200))
    val b = a.map(x => x._2)
    val c = b.asInstanceOf[ArrayBuffer[String]]
    GkConsoleLogger.info("下载处理者: 获得下载地址" + c(0) + "...")
    c(0)+filePath
  }
  def testRepo(filrUrl:String): Int ={
    val url = new URL(filrUrl)
    val conn = url.openConnection().asInstanceOf[HttpURLConnection];
    conn.setConnectTimeout(2000)

    try{
      conn.getResponseCode
    }catch {
     case ex:Exception => println(ex.getMessage); 0

    }

  }

  override def receive: Receive = {
    case filepath:String =>{
      GkConsoleLogger.info("下载处理者: 收到下载请求...")
      GkConsoleLogger.info("下载处理者: 开始寻找文件所在仓库...")
      sourceR(filepath)
      GkConsoleLogger.info("下载处理者: 下载完成...")
    }
  }

 def sourceR(filePath:String) {
   val downurl = getFileDownUrl(filePath)
   GkConsoleLogger.info(".................."+downurl+"..............................")
   import java.net.{HttpURLConnection, URL};
   val downUrl = new URL(downurl)
   val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
   downConn.setConnectTimeout(5000);
   val fileLength = downConn.getContentLength
   val downIs = downConn.getInputStream();

   val fileOs = cfg.getLocalRepositoryDir + filePath
   GkConsoleLogger.info("下载处理者: 下载完成..."+fileOs)
   val file = new File(fileOs)
   if (!file.getParentFile.exists()) {
     file.getParentFile.mkdirs()
   }
   val raf = new RandomAccessFile(fileOs, "rwd");
   val buffer = new Array[Byte](fileLength)
   raf.setLength(fileLength);


   var tot = 0
   var start = 0
   var len = 0
   while (len != -1 && fileLength != tot) {
     len = downIs.read(buffer, start, fileLength - tot)
     start += len
     tot += len
     println(tot)
   }
   println("下载完成:" + tot)
   raf.write(buffer)
   downIs.close()
   raf.close();
 }
}


