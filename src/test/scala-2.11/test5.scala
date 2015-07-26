/**
 * Created by goku on 2015/7/24.
 */
object test5 {
  def main(args: Array[String]) {
//    val headFirstLine = "GET /a aaa"
    val headFirstLine = ("b",1)
    headFirstLine match {
      case ("a",b:Int) => println("null")
      case ("b",b:Int) => println("null1")
      case _ => println("aaaa")
    }
  }
}
