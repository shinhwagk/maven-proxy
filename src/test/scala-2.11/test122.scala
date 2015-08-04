import org.gk.db.MetaData._
import org.gk.db.{InitDatabase, Tables}
import slick.dbio.DBIO
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import slick.driver.H2Driver.api._
import slick.jdbc.meta._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by goku on 2015/8/3.
 */
object abc{
  def main(args: Array[String]) {
    val db = Database.forConfig("DownFileWorkers")
    import org.gk.db.Tables._
    val a = Await.result(db.run(downFileList.map(p => (p.WorksNumber)).result),Duration).head
    println(a)

  }
}