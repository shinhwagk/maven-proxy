package org.gk.db

import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created by goku on 2015/8/3.
 */
object InitDatabase {

  import MetaData._

  def initTable: Unit ={
    if(tables.isEmpty)
      createTable
  }

  def createTable: Unit = {
    import org.gk.db.Tables._
    val setup = DBIO.seq(
      downFileList.schema.create,
      downFileWorkList.schema.create
    )
    Await.result(db.run(setup),Duration.Inf)
  }
}
