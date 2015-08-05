package org.gk.db
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

  def insertDownMaster(file:String,fileUrl:String,WorksNumber:Int): Unit ={
    println(file+"被插入到数据库")
    println()
    val insert = DBIO.seq(
      downFileList += (file,fileUrl,WorksNumber)
    )

    Await.result(db.run(insert), Duration.Inf)
  }

  def insertDownWorker(file:String,fileUrl:String,startIndex:Int,enIndex:Int,success:Int): Unit ={
//    println(file+"被插入到数据库work")
    val insert = DBIO.seq(
      downFileWorkList += (file,fileUrl,startIndex,enIndex,success)
    )

    Await.result(db.run(insert), Duration.Inf)

  }

  def updateDownWorker(fileUrl:String,startIndex:Int): Unit = {

      Await.result(db.run(downFileWorkList.filter(_.fileUrl === fileUrl).filter(_.startIndex === startIndex).map(p => (p.success)).update(1)), Duration.Inf)



  }

  def deleteDownWorker(fileUrl:String): Unit ={
    val delete = DBIO.seq(
      downFileWorkList.filter(_.fileUrl === fileUrl).delete
    )
    Await.result(db.run(delete), Duration.Inf)
  }

  def deleteDownfile(fileUrl:String): Unit ={
    val delete = DBIO.seq(
      downFileList.filter(_.fileUrl === fileUrl).delete
    )
    Await.result(db.run(delete), Duration.Inf)
  }

  def countDownSuccessNumber(fileUrl:String) :Int ={
      Await.result(db.run(downFileWorkList.filter(_.fileUrl === fileUrl).map(p => (p.success)).result),Duration.Inf).toList.sum
  }

  def selectDownNumber(fileUrl:String) : Int = {
    Await.result(db.run(downFileList.filter(_.fileUrl === fileUrl).map(p => (p.WorksNumber)).result),Duration.Inf).head
  }

}