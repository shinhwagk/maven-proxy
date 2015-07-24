
/**
 * Created by goku on 2015/7/24.
 */
object test3 {
  def main(args: Array[String]) {
    import java.io.File
    val filepath = "Z://mavenR/HTTPClient/HTTPClient/0.3-3/HTTPClient-0.3-3.jar"
    val file = new File("Z://mavenR/HTTPClient/HTTPClient/0.3-3/HTTPClient-0.3-3.jar");

    println(file.getParentFile.mkdirs())

  }
}
