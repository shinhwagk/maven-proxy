/**
 * Created by goku on 2015/7/24.
 */
object test2 {
  def main(args: Array[String]) {
    val b = "aaa"
    case class a(a:Int)
    b match {
      case sh:String => println(sh)
      case _ => println("ccc")
    }

  }
}
