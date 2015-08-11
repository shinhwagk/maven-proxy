package org.gk.server.db

import org.gk.server.config.cfg
import org.gk.server.db.Tables._
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

  def initMavenProxy: Unit = {
    if (tables.isEmpty) {
      createTable
      initRepository
    }
  }

  private def createTable: Unit = {
    import Tables._
    val setup = DBIO.seq(
      downFileList.schema.create,
      downFileWorkList.schema.create,
      repositoryTable.schema.create
    )
    Await.result(db.run(setup), Duration.Inf)
  }

  private def initRepository: Unit = {
    import Tables._
    val insert = DBIO.seq(
      repositoryTable +=("central", "https://repo.maven.apache.org/maven2", 1, true)
    )
    Await.result(db.run(insert), Duration.Inf)
  }
}
