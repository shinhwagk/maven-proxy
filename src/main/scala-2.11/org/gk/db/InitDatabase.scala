package org.gk.db

import org.gk.config.cfg
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
    if(tables.isEmpty) {
      createTable
      loadRepository
    }
  }

  def createTable: Unit = {
    import org.gk.db.Tables._
    val setup = DBIO.seq(
      downFileList.schema.create,
      downFileWorkList.schema.create,
      repositoryTable.schema.create
    )
    Await.result(db.run(setup),Duration.Inf)
  }

  def loadRepository ={
    val repoList = cfg.getRemoteRepoMap
    import org.gk.db.DML._
    repoList.map(l => {
      val repoName = l._1
      val repoUrl = l._2.url
      val repoPort = l._2.port
      insertRepository(repoName,repoUrl,repoPort,false)
    })
  }
}
