package org.gk.server



/**
 * Created by goku on 2015/8/19.
 */
object HttpProxy {
  def main(args: Array[String]) {
    import java.net.{HttpURLConnection, URL}
    println("x")
    val httpConn = new URL("https://codeload.github.com/apache/incubator-zeppelin/zip/master").openConnection.asInstanceOf[HttpURLConnection]
    httpConn.setRequestProperty("Range", "bytes=1-100")
    println(httpConn.getResponseCode)
  }
}
