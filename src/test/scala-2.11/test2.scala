/**
 * Created by goku on 2015/7/24.
 */
object test2 {
  def main(args: Array[String]) {
    case class a(a:Int)
    a(1) match {
      case sh => println(sh)
      case _ => println("ccc")
    }

  }
}
