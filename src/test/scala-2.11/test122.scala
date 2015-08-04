import org.gk.db.{InitDatabase, Tables}
import slick.dbio.DBIO
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import slick.driver.H2Driver.api._
import slick.jdbc.meta._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by goku on 2015/8/3.
 */
object abc{
  def main(args: Array[String]) {
    val db = Database.forConfig("DownFileWorkers")
    val tables = db.run(MTable.getTables)
  }
}