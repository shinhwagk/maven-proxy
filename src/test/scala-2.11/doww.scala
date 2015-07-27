import java.io.RandomAccessFile
import java.net.{URL, HttpURLConnection}

import akka.actor.{Actor, Props, ActorSystem}
import akka.actor.Actor.Receive
import akka.routing.RoundRobinPool
import org.gk.config.cfg

/**
 * Created by goku on 2015/7/27.
 */

object abc {
  def main(args: Array[String]) {

    val system = ActorSystem("PiSystem")
    val doww = system.actorOf(Props[doww],name ="work")
    doww ! "a"
  }
}
class doww extends Actor with akka.actor.ActorLogging{
  val processNum = cfg.getDownFilePorcessNumber
  val work = context.actorOf(RoundRobinPool(10).props(Props[Worker]),name ="work")



  def downFile11(fileUrl:String): Unit ={
    val httpConn = getHttpConn(fileUrl)
    val fileLength = httpConn.getContentLength
    val raf = new RandomAccessFile("Z:\\apache-14.pom.sha1111", "rwd");
    raf.setLength(fileLength);
    raf.close()

    println(fileLength)

    val endLength = fileLength%processNum
    val step = (fileLength-endLength)/processNum

    for (thread <- 1 to processNum) {
      thread match {
        case _ if thread == processNum =>{
          work ! Work(fileUrl,thread,(thread - 1)*step,thread*step+endLength )
//          log.debug("线程: {} 下载请求已经发送...",thread)
        }
        case _ =>{
          work ! Work(fileUrl,thread,(thread - 1)*step,thread*step-1)
//          log.debug("线程: {} 下载请求已经发送...",thread)
        }
      }
    }
  }
  def getHttpConn(Url:String): HttpURLConnection ={
    import java.net.{HttpURLConnection, URL};
    val downUrl = new URL(Url)
    downUrl.openConnection().asInstanceOf[HttpURLConnection];
  }

  override def receive: Actor.Receive = {
    case "a" =>{
      downFile11("https://repo1.maven.org/maven2/org/apache/apache/14/apache-14.pom.sha1")
    }
  }
}

case class Work(url:String,thread:Int,startIndex:Int, endIndex:Int)
class Worker extends Actor with akka.actor.ActorLogging{
  override def receive: Actor.Receive = {
    case Work(url,thread,startIdex,endIndex) => {
//      log.debug("线程: {} 下载请求收到,开始下载...",thread)
      down(url,thread,startIdex,endIndex)
//      log.debug("线程: {} 下载完毕...",thread)
    };
  }

  def down(url:String,thread:Int,startIndex:Int, endIndex:Int) {
    log.debug("线程: {},需要下载 {} bytes ...",thread,endIndex-startIndex)
    val downUrl = new URL(url)
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
    val is = downConn.getInputStream();
    val workFileLength = downConn.getContentLength
    val raf =  new RandomAccessFile("Z:\\apache-14.pom.sha1111", "rwd");
    raf.seek(startIndex);

    var currentLength = 0
    var start = 0
    var len = 0

    val buffer = new Array[Byte](workFileLength)
    while (len != -1 && workFileLength != currentLength) {
      len = is.read(buffer, start, workFileLength - currentLength)
      start += len
      currentLength += len
      println(currentLength + "/" + workFileLength)
      log.info("线程: {}下载进度 {}/{} 下载完毕...",thread,currentLength,workFileLength)
    }
    raf.write(buffer)
    is.close()
    raf.close()
    println("下载完毕")
  }
}
