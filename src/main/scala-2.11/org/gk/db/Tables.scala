package org.gk.db

import slick.driver.H2Driver.api._

/**
 * Created by goku on 2015/8/3.
 */
object Tables {

  class DownFileList(tag: Tag) extends Table[(String, String, Int)](tag, "DOWN_FILE_LIST") {
    def file = column[String]("FILE", O.PrimaryKey)

    def fileUrl = column[String]("FILE_URL")

    def WorksNumber = column[Int]("WORK_NUMBER")

    def * = (file, fileUrl, WorksNumber)
  }

  val downFileList = TableQuery[DownFileList]

  class DownFileWorkList(tag: Tag) extends Table[(String, String, Int, Int, Int, Int)](tag, "DOWN_FILE_WORK_LIST") {
    def file = column[String]("FILE")

    def fileUrl = column[String]("FILE_URL")

    def startIndex = column[Int]("START_INDEX")

    def enIndex = column[Int]("END_INDEX")

    def success = column[Int]("SUCCESS")

    def restartCount = column[Int]("Restart_Count")

    //    def reStartCount = column[Int]("SUCCESS")

    def * = (file, fileUrl, startIndex, enIndex, success, restartCount)
  }

  val downFileWorkList = TableQuery[DownFileWorkList]
}
