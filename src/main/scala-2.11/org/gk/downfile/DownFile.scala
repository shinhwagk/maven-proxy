package org.gk.downfile

import java.io._
import java.util.Date

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.gk.config.cfg
import org.gk.log.GkConsoleLogger

/**
 * Created by goku on 2015/7/22.
 */
class DownFile {
  /*
  def main(args: Array[String]) {
    case class DownAccomplish()

    //文件路径
    val filepath = "/org/apache/apache/17/apache-17.pom"
    //公共仓库列表
    val ReositoryConfig = Source.fromFile("Repository.cfg")
    val a = mirrorList.filter(x=>ceshidizhi(x+fileaddress) == 200)
    println(ceshidizhi(mirrorList.head))


    def ceshidizhi(dizhi:String):Int= {
        val url = new URL(dizhi)
        val conn = url.openConnection().asInstanceOf[HttpURLConnection];
        conn.getResponseCode()
      }
//    conn.setRequestMethod("GET");
    if(conn.getResponseCode() == 200) {
      val length = conn.getContentLength();
      println(length)
      val raf = new RandomAccessFile("HTTPClient-0.3-3.jar", "rwd");
      raf.setLength(length);
      raf.close()
    }
  }



  def downMvnFile(fileUrl:String,downThreadNum:Int): Unit ={
    val system = ActorSystem("DownMvnFile")

    val listener = system.actorOf(Props[Listener], name = "listener")

    val master = system.actorOf(Props(new Master(fileUrl, downThreadNum, listener)),name = "master")
  }


*/



 def sourceR(filePath:String) {
   println(filePath)
   import java.net.{HttpURLConnection, URL};
   var repositoryName =""
   var fileUrl = ""
   var fileLength = 0
   val repositoryMap = org.gk.config.cfg.getRepositoryMap
   for ((k, v) <- repositoryMap) {
     val url = new URL(v + filePath)
     val conn = url.openConnection().asInstanceOf[HttpURLConnection];
     if (conn.getResponseCode == 200) {
       repositoryName = k
       fileUrl = v + filePath
     }
     else {
       println("a")
     }
     //获得文件长度
     fileLength = conn.getContentLength;

   }
   val downUrl = new URL(fileUrl)
   val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
   downConn.setConnectTimeout(5000);
   val downIs = downConn.getInputStream();

   val jarpath = cfg.getLocalRepositoryDir + filePath

   val file = new File(jarpath)
   println(jarpath)
   if (file.exists()) {
     println("文件删除结果" + file.delete())
   }else{
     file.getParentFile.mkdirs()
   }
   val raf = new RandomAccessFile(cfg.getLocalRepositoryDir + filePath, "rwd");
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

/*

    val file = new File("Z:\\mavenR\\HTTPClient-0.3-3.jar")
    var fis = new FileInputStream(file);
    var bis = new BufferedInputStream(fis);
    var bislength = bis.available();
    var os = socket.getOutputStream();
    var bos = new BufferedOutputStream(os);
    var sb = new StringBuilder();
    sb.append("HTTP/1.1 200 OK\n");
    sb.append("Content-Type: application/java-archive\n");
    sb.append("Content-Type: application/octet-stream\n");
    sb.append("Date: " + new Date() + "\n");
    sb.append("Content-Length: " + (bislength) + "\n");
    sb.append("Accept-Ranges: bytes\n");
    sb.append("Connection: Keep-Alive\n")
    sb.append("Keep-Alive: true\n");

    sb.append("\n");
    bos.write(sb.toString().getBytes);
    println("文件长度" + bislength)
    var buffer = new Array[Byte](bislength);
    bis.read(buffer, 0, bislength);
    bos.write(buffer); // 返回文件数据
    bos.flush();
    bos.close();
    bis.close()
    socket.close();
  }*/
//  override def receive: Receive = {
//    case path:String =>{
//      GkConsoleLogger.info("下载处理者: 开始寻找文件所在仓库...")
//      sourceR(path)
//      GkConsoleLogger.info("下载处理者: 下载完成...")
//      sender() ! "Down Ok"
//    }
//  }
}
