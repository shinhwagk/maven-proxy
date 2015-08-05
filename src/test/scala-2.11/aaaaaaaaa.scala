import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}

/**
 * Created by goku on 2015/8/5.
 */
class aaaaaaaaa {

}


object hjdghdf{
  def main(args: Array[String]) {
    val downUrl = new URL("https://repo.maven.apache.org/maven2/org/scala-lang/scala-library/2.10.4/scala-library-2.10.4-javadoc.jar");
    val connection  = downUrl.openConnection().asInstanceOf[HttpURLConnection];
    connection.setDoOutput(true);
    connection.setDoInput(true);
    val reader  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
      println(reader.readLine());
    reader.close();

  }
}