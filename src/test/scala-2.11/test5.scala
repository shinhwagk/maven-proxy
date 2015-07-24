/**
 * Created by goku on 2015/7/24.
 */
object test5 {
  def main(args: Array[String]) {
    val a = for (i <- (1 to 10)) yield i
    println(a)
  }
}
