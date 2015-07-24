package org.gk.downfile

import java.io._
import java.util.Date

/**
 * Created by goku on 2015/7/22.
 */
object DownMvnFile {
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

  class Master(fileUrl:String,downThreadNum: Int, listener: ActorRef) extends Actor {
    val workerRouter = context.actorOf(RoundRobinPool(5).props(Props[Worker]), "workerRouter")
    override def receive: Receive = ???
  }
  class Worker extends Actor {

    def down(startIndex:Int, endIndex:Int): Double = {
      var acc = 0.0
      for (i ← start until (start + nrOfElements))
        acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
      acc
    }

    def receive = {
      case Work(start, nrOfElements) ⇒
        sender ! Result(down(start, nrOfElements)) // perform the work
    }
  }

  def downMvnFile(fileUrl:String,downThreadNum:Int): Unit ={
    val system = ActorSystem("DownMvnFile")

    val listener = system.actorOf(Props[Listener], name = "listener")

    val master = system.actorOf(Props(new Master(fileUrl, downThreadNum, listener)),name = "master")
  }

  class Listener extends Actor {
    def receive = {
      case DownAccomplish =>
        println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s".format("a", "a"))
        context.system.shutdown()
    }
  }
*/
/*
  import java.net.{HttpURLConnection, URL};
  var repositoryName =""
  var fileUrl = ""
  var fileLength = 0

  if(!localFileExists){
    val repositoryMap = org.gk.config.cfg.getRepositoryMap
    for ((k,v) <- repositoryMap){

      val url = new URL(v + filePath)
      val conn = url.openConnection().asInstanceOf[HttpURLConnection];
      if( conn.getResponseCode == 200 ) {
        repositoryName = k
        fileUrl = v + filePath
      }
      else{
        println("a")
      }
      //获得文件长度
      fileLength = conn.getContentLength;
    }
    println(repositoryName)
    println(fileUrl)
    val downUrl = new URL(fileUrl)
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000);
    val downIs = downConn.getInputStream();
    val file = new File("Z://HTTPClient-0.3-3.jar")
    if(file.exists()){
      println("文件删除结果"+ file.delete())
    }
    val raf = new RandomAccessFile("Z://HTTPClient-0.3-3.jar", "rwd");
    val buffer = new Array[Byte](fileLength)
    raf.setLength(fileLength);


    var tot = 0
    var start = 0
    var len = 0
    while(len != -1 && fileLength != tot){
      len = downIs.read(buffer,start,fileLength-tot)
      start += len
      tot += len
      println(tot)
    }
    println("下载完成:"+tot)
    raf.write(buffer)
    downIs.close()
    raf.close();



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
}
