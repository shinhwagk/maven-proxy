import java.net.{HttpURLConnection, URL}

/**
 * Created by goku on 2015/7/23.
 */
object test {
  def main(args: Array[String]) {
    val downUrl = new URL("http://127.0.0.1:8082/HTTPClient-0.3-3.jar")
    val downConn = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    downConn.setConnectTimeout(5000);
    println(downConn.getResponseCode)

  }
}
