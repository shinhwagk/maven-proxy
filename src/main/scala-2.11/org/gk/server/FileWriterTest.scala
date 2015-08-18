package org.gk.server

import java.io.{File, FileWriter}

import org.gk.server.workers.Headers
/**
 * Created by goku on 2015/8/18.
 */
object FileWriterTest {
  val file = new File("./aaa")
  val writer = new FileWriter(file.getName, true);
  def insert(headers:Headers): Unit ={
    writer.write(headers.Head_Path.get)
    headers.headText.split("\r\n").foreach(writer.write)
  }
}
