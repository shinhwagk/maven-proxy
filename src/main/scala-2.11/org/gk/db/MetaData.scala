package org.gk.db

import slick.dbio.{DBIO, NoStream, DBIOAction}
import slick.jdbc.meta.MTable
import slick.driver.H2Driver.api._
import scala.concurrent.Await
import scala.concurrent.duration._
/**
 * Created by goku on 2015/8/3.
 */
object MetaData {
  val db = Database.forConfig("h2mem1")

  val tables = Await.result(db.run(MTable.getTables), Duration.Inf).toList

}
