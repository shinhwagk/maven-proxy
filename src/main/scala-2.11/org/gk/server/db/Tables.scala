package org.gk.server.db

import slick.driver.H2Driver.api._

/**
 * Created by goku on 2015/8/3.
 */
object Tables {

  class Repository(tag: Tag) extends Table[(String, String, Int, Boolean)](tag, "REPOSITORY") {
    def name = column[String]("Name", O.PrimaryKey)

    def url = column[String]("URL")

    //优先级
    def priority = column[Int]("PRIORITY")

    def start = column[Boolean]("START")

    def * = (name, url, priority, start)
  }

  val repositoryTable = TableQuery[Repository]
}
