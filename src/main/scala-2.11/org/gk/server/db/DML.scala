package org.gk.server.db

import slick.driver.H2Driver.api._
import slick.dbio.DBIO
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import scala.concurrent.Await

/**
 * Created by goku on 2015/8/3.
 */
object DML {

  import MetaData._
  import Tables._

   def addRepository(repoName:String,repoUrl:String,priority:Int,start:Boolean): Unit ={
    Await.result(db.run(DBIO.seq(repositoryTable += (repoName,repoUrl,priority,start,0,0))), Duration.Inf)
  }

  def deleteRepository(repoName:String): Unit ={
    Await.result(db.run(DBIO.seq(repositoryTable.filter(_.name === repoName).delete)), Duration.Inf)
  }

  def listRepoitory:List[(String,String,Int,Boolean,Int,Int)] = {
    Await.result(db.run(repositoryTable.result), Duration.Inf).toList
  }
}
